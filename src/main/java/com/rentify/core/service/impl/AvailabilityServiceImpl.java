package com.rentify.core.service.impl;

import com.rentify.core.dto.property.AvailabilityBlockDto;
import com.rentify.core.dto.property.AvailabilityBlockRequestDto;
import com.rentify.core.dto.property.UnavailableDateRangeDto;
import com.rentify.core.entity.AvailabilityBlock;
import com.rentify.core.entity.Booking;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.User;
import com.rentify.core.enums.BookingStatus;
import com.rentify.core.config.AvailabilityProperties;
import com.rentify.core.exception.DomainException;
import com.rentify.core.mapper.AvailabilityMapper;
import com.rentify.core.repository.AvailabilityBlockRepository;
import com.rentify.core.repository.BookingRepository;
import com.rentify.core.repository.PropertyRepository;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.AvailabilityService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final AvailabilityBlockRepository availabilityRepository;
    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final AuthenticationService authService;
    private final AvailabilityMapper mapper;
    private final AvailabilityProperties availabilityProperties;

    @Override
    @Transactional
    public AvailabilityBlockDto createBlock(Long propertyId, AvailabilityBlockRequestDto request) {
        if (request.dateFrom().isAfter(request.dateTo())) {
            throw DomainException.badRequest("DATE_RANGE_INVALID", "dateFrom must be before or equal to dateTo");
        }
        Property property = propertyRepository.findByIdForUpdate(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));
        User currentUser = authService.getCurrentUser();
        if (!property.getHost().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not the host of this property");
        }
        List<AvailabilityBlock> overlappingBlocks = availabilityRepository
                .findAllByPropertyIdAndDateFromLessThanEqualAndDateToGreaterThanEqual(
                        propertyId, request.dateTo(), request.dateFrom());
        if (!overlappingBlocks.isEmpty()) {
            throw DomainException.conflict("AVAILABILITY_ALREADY_BLOCKED", "These dates are already blocked.");
        }
        boolean hasBookings = bookingRepository.hasOverlappingBookings(
                propertyId, request.dateFrom(), request.dateTo(),
                List.of(BookingStatus.CANCELLED, BookingStatus.REJECTED, BookingStatus.COMPLETED)
        );
        if (hasBookings) {
            throw DomainException.conflict("AVAILABILITY_HAS_BOOKINGS", "Cannot block dates because there are existing bookings for this period.");
        }
        AvailabilityBlock block = AvailabilityBlock.builder()
                .property(property)
                .dateFrom(request.dateFrom())
                .dateTo(request.dateTo())
                .reason(request.reason())
                .createdBy(currentUser)
                .build();
        return mapper.toDto(availabilityRepository.save(block));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityBlockDto> getBlocksByProperty(Long propertyId) {
        return mapper.toDtos(availabilityRepository.findAllByPropertyId(propertyId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnavailableDateRangeDto> getUnavailableRangesByProperty(Long propertyId, LocalDate dateFrom, LocalDate dateTo) {
        validateDateRange(dateFrom, dateTo);

        if (!propertyRepository.existsById(propertyId)) {
            throw new EntityNotFoundException("Property not found");
        }

        List<AvailabilityBlock> blocks;
        List<Booking> bookings;
        List<BookingStatus> excludedStatuses = List.of(
                BookingStatus.CANCELLED,
                BookingStatus.REJECTED,
                BookingStatus.COMPLETED
        );

        if (dateFrom != null) {
            blocks = availabilityRepository.findAllByPropertyIdAndDateFromLessThanEqualAndDateToGreaterThanEqual(
                    propertyId, dateTo, dateFrom
            );
            bookings = fetchAllOverlappingBookings(propertyId, excludedStatuses, dateFrom, dateTo);
        } else {
            blocks = availabilityRepository.findAllByPropertyId(propertyId);
            bookings = fetchAllBookingsByPropertyExcludingStatuses(propertyId, excludedStatuses);
        }

        List<UnavailableDateRangeDto> unavailableRanges = new ArrayList<>();
        unavailableRanges.addAll(mapper.toUnavailableDateRangeDtosFromBlocks(blocks));
        unavailableRanges.addAll(mapper.toUnavailableDateRangeDtosFromBookings(bookings));

        unavailableRanges.sort(
                Comparator.comparing(UnavailableDateRangeDto::dateFrom)
                        .thenComparing(UnavailableDateRangeDto::dateTo)
        );

        return unavailableRanges;
    }

    @Override
    @Transactional
    public void deleteBlock(Long propertyId, Long blockId) {
        AvailabilityBlock block = availabilityRepository.findById(blockId)
                .orElseThrow(() -> new EntityNotFoundException("Block not found"));
        User currentUser = authService.getCurrentUser();
        if (!block.getProperty().getId().equals(propertyId) ||
                !block.getProperty().getHost().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this block");
        }
        availabilityRepository.delete(block);
    }

    private void validateDateRange(LocalDate dateFrom, LocalDate dateTo) {
        if ((dateFrom == null) != (dateTo == null)) {
            throw DomainException.badRequest("DATE_RANGE_INVALID", "Both dateFrom and dateTo must be provided together.");
        }
        if (dateFrom != null && !dateFrom.isBefore(dateTo)) {
            throw DomainException.badRequest("DATE_RANGE_INVALID", "dateFrom must be before dateTo.");
        }
    }

    private List<Booking> fetchAllOverlappingBookings(
            Long propertyId,
            List<BookingStatus> excludedStatuses,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        List<Booking> result = new ArrayList<>();
        Page<Booking> page;
        int pageNumber = 0;
        do {
            page = bookingRepository.findOverlappingByPropertyIdAndStatusNotIn(
                    propertyId,
                    excludedStatuses,
                    dateFrom,
                    dateTo,
                    PageRequest.of(pageNumber, availabilityProperties.getBookingFetchPageSize(), Sort.by(Sort.Direction.ASC, "dateFrom"))
            );
            result.addAll(page.getContent());
            pageNumber++;
        } while (page.hasNext());
        return result;
    }

    private List<Booking> fetchAllBookingsByPropertyExcludingStatuses(
            Long propertyId,
            List<BookingStatus> excludedStatuses
    ) {
        List<Booking> result = new ArrayList<>();
        Page<Booking> page;
        int pageNumber = 0;
        do {
            page = bookingRepository.findAllByPropertyIdAndStatusNotIn(
                    propertyId,
                    excludedStatuses,
                    PageRequest.of(pageNumber, availabilityProperties.getBookingFetchPageSize(), Sort.by(Sort.Direction.ASC, "dateFrom"))
            );
            result.addAll(page.getContent());
            pageNumber++;
        } while (page.hasNext());
        return result;
    }
}
