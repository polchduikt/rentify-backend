package com.rentify.core.service.impl;

import com.rentify.core.dto.AvailabilityBlockDto;
import com.rentify.core.dto.AvailabilityBlockRequestDto;
import com.rentify.core.entity.AvailabilityBlock;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.User;
import com.rentify.core.enums.BookingStatus;
import com.rentify.core.mapper.AvailabilityMapper;
import com.rentify.core.repository.AvailabilityBlockRepository;
import com.rentify.core.repository.BookingRepository;
import com.rentify.core.repository.PropertyRepository;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.AvailabilityService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final AvailabilityBlockRepository availabilityRepository;
    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final AuthenticationService authService;
    private final AvailabilityMapper mapper;

    @Override
    @Transactional
    public AvailabilityBlockDto createBlock(Long propertyId, AvailabilityBlockRequestDto request) {
        if (request.dateFrom().isAfter(request.dateTo())) {
            throw new IllegalArgumentException("dateFrom must be before or equal to dateTo");
        }
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));
        User currentUser = authService.getCurrentUser();
        if (!property.getHost().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not the host of this property");
        }
        List<AvailabilityBlock> overlappingBlocks = availabilityRepository
                .findAllByPropertyIdAndDateFromLessThanEqualAndDateToGreaterThanEqual(
                        propertyId, request.dateTo(), request.dateFrom());
        if (!overlappingBlocks.isEmpty()) {
            throw new IllegalStateException("These dates are already blocked.");
        }
        boolean hasBookings = bookingRepository.hasOverlappingBookings(
                propertyId, request.dateFrom(), request.dateTo(),
                List.of(BookingStatus.CANCELLED, BookingStatus.REJECTED)
        );
        if (hasBookings) {
            throw new IllegalStateException("Cannot block dates because there are existing bookings for this period.");
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
        return availabilityRepository.findAllByPropertyId(propertyId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
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
}