package com.rentify.core.unit;

import com.rentify.core.dto.property.AvailabilityBlockDto;
import com.rentify.core.dto.property.AvailabilityBlockRequestDto;
import com.rentify.core.dto.property.UnavailableDateRangeDto;
import com.rentify.core.entity.AvailabilityBlock;
import com.rentify.core.entity.Booking;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.User;
import com.rentify.core.enums.BookingStatus;
import com.rentify.core.mapper.AvailabilityMapper;
import com.rentify.core.repository.AvailabilityBlockRepository;
import com.rentify.core.repository.BookingRepository;
import com.rentify.core.repository.PropertyRepository;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.impl.AvailabilityServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceImplTest {

    @Mock private AvailabilityBlockRepository availabilityRepository;
    @Mock private PropertyRepository propertyRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private AuthenticationService authService;
    @Mock private AvailabilityMapper mapper;

    @InjectMocks
    private AvailabilityServiceImpl availabilityService;

    private Property property;
    private User host;
    private User otherUser;
    private AvailabilityBlockRequestDto request;

    @BeforeEach
    void setUp() {
        host = User.builder().id(1L).build();
        otherUser = User.builder().id(2L).build();

        property = Property.builder()
                .id(10L)
                .host(host)
                .build();

        request = new AvailabilityBlockRequestDto(
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 4, 15),
                "Maintenance"
        );
    }

    @Nested
    @DisplayName("createBlock()")
    class CreateBlockTests {

        @Test
        void shouldThrowIllegalArgument_whenDateRangeInvalid() {
            AvailabilityBlockRequestDto invalid = new AvailabilityBlockRequestDto(
                    LocalDate.of(2026, 4, 15),
                    LocalDate.of(2026, 4, 10),
                    "invalid"
            );

            assertThatThrownBy(() -> availabilityService.createBlock(10L, invalid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("dateFrom must be before or equal to dateTo");
        }

        @Test
        void shouldThrowEntityNotFound_whenPropertyMissing() {
            when(propertyRepository.findByIdForUpdate(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> availabilityService.createBlock(10L, request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Property not found");
        }

        @Test
        void shouldThrowAccessDenied_whenCurrentUserIsNotHost() {
            when(propertyRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(property));
            when(authService.getCurrentUser()).thenReturn(otherUser);

            assertThatThrownBy(() -> availabilityService.createBlock(10L, request))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You are not the host of this property");
        }

        @Test
        void shouldThrowIllegalState_whenOverlappingBlockExists() {
            when(propertyRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(property));
            when(authService.getCurrentUser()).thenReturn(host);
            when(availabilityRepository.findAllByPropertyIdAndDateFromLessThanEqualAndDateToGreaterThanEqual(
                    10L, request.dateTo(), request.dateFrom()
            )).thenReturn(List.of(AvailabilityBlock.builder().id(1L).build()));

            assertThatThrownBy(() -> availabilityService.createBlock(10L, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("These dates are already blocked.");
        }

        @Test
        void shouldThrowIllegalState_whenOverlappingBookingsExist() {
            when(propertyRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(property));
            when(authService.getCurrentUser()).thenReturn(host);
            when(availabilityRepository.findAllByPropertyIdAndDateFromLessThanEqualAndDateToGreaterThanEqual(
                    10L, request.dateTo(), request.dateFrom()
            )).thenReturn(List.of());
            when(bookingRepository.hasOverlappingBookings(eq(10L), eq(request.dateFrom()), eq(request.dateTo()), anyList()))
                    .thenReturn(true);

            assertThatThrownBy(() -> availabilityService.createBlock(10L, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot block dates because there are existing bookings for this period.");
        }

        @Test
        void shouldCreateBlock_whenRequestIsValid() {
            ZonedDateTime createdAt = ZonedDateTime.now();
            AvailabilityBlock saved = AvailabilityBlock.builder()
                    .id(50L)
                    .property(property)
                    .dateFrom(request.dateFrom())
                    .dateTo(request.dateTo())
                    .reason(request.reason())
                    .createdBy(host)
                    .build();
            saved.setCreatedAt(createdAt);
            AvailabilityBlockDto dto = new AvailabilityBlockDto(
                    50L, 10L, request.dateFrom(), request.dateTo(), request.reason(), 1L, createdAt
            );

            when(propertyRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(property));
            when(authService.getCurrentUser()).thenReturn(host);
            when(availabilityRepository.findAllByPropertyIdAndDateFromLessThanEqualAndDateToGreaterThanEqual(
                    10L, request.dateTo(), request.dateFrom()
            )).thenReturn(List.of());
            when(bookingRepository.hasOverlappingBookings(eq(10L), eq(request.dateFrom()), eq(request.dateTo()), anyList()))
                    .thenReturn(false);
            when(availabilityRepository.save(any(AvailabilityBlock.class))).thenReturn(saved);
            when(mapper.toDto(saved)).thenReturn(dto);

            AvailabilityBlockDto result = availabilityService.createBlock(10L, request);
            ArgumentCaptor<AvailabilityBlock> captor = ArgumentCaptor.forClass(AvailabilityBlock.class);

            assertThat(result.id()).isEqualTo(50L);
            verify(availabilityRepository).save(captor.capture());
            assertThat(captor.getValue().getProperty()).isEqualTo(property);
            assertThat(captor.getValue().getCreatedBy()).isEqualTo(host);
            assertThat(captor.getValue().getReason()).isEqualTo("Maintenance");
        }
    }

    @Nested
    @DisplayName("getBlocksByProperty()")
    class GetBlocksByPropertyTests {

        @Test
        void shouldReturnMappedBlocks() {
            AvailabilityBlock block = AvailabilityBlock.builder().id(10L).build();
            AvailabilityBlockDto dto = new AvailabilityBlockDto(
                    10L, 10L, LocalDate.now(), LocalDate.now().plusDays(1), "reason", 1L, ZonedDateTime.now()
            );
            when(availabilityRepository.findAllByPropertyId(10L)).thenReturn(List.of(block));
            when(mapper.toDtos(List.of(block))).thenReturn(List.of(dto));

            List<AvailabilityBlockDto> result = availabilityService.getBlocksByProperty(10L);

            assertThat(result).containsExactly(dto);
        }
    }

    @Nested
    @DisplayName("getUnavailableRangesByProperty()")
    class GetUnavailableRangesTests {

        @Test
        void shouldThrowIllegalArgument_whenOnlyOneDateProvided() {
            assertThatThrownBy(() -> availabilityService.getUnavailableRangesByProperty(10L, LocalDate.now(), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Both dateFrom and dateTo must be provided together.");
        }

        @Test
        void shouldThrowIllegalArgument_whenDateFromNotBeforeDateTo() {
            LocalDate date = LocalDate.of(2026, 5, 1);

            assertThatThrownBy(() -> availabilityService.getUnavailableRangesByProperty(10L, date, date))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("dateFrom must be before dateTo.");
        }

        @Test
        void shouldThrowEntityNotFound_whenPropertyMissing() {
            when(propertyRepository.existsById(10L)).thenReturn(false);

            assertThatThrownBy(() -> availabilityService.getUnavailableRangesByProperty(
                    10L, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10)
            )).isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Property not found");
        }

        @Test
        void shouldReturnSortedCombinedRanges_whenDateRangeProvided() {
            LocalDate from = LocalDate.of(2026, 5, 1);
            LocalDate to = LocalDate.of(2026, 5, 10);
            AvailabilityBlock block = AvailabilityBlock.builder()
                    .dateFrom(LocalDate.of(2026, 5, 3))
                    .dateTo(LocalDate.of(2026, 5, 4))
                    .build();
            Booking booking = Booking.builder()
                    .dateFrom(LocalDate.of(2026, 5, 1))
                    .dateTo(LocalDate.of(2026, 5, 2))
                    .status(BookingStatus.CONFIRMED)
                    .build();

            when(propertyRepository.existsById(10L)).thenReturn(true);
            when(availabilityRepository.findAllByPropertyIdAndDateFromLessThanEqualAndDateToGreaterThanEqual(10L, to, from))
                    .thenReturn(List.of(block));
            when(bookingRepository.findOverlappingByPropertyIdAndStatusNotIn(
                    eq(10L), anyList(), eq(from), eq(to), any()
            )).thenReturn(new PageImpl<>(List.of(booking)));
            when(mapper.toUnavailableDateRangeDtosFromBlocks(List.of(block))).thenReturn(List.of(
                    new UnavailableDateRangeDto(
                            block.getDateFrom(),
                            block.getDateTo(),
                            "BLOCK",
                            null
                    )
            ));
            when(mapper.toUnavailableDateRangeDtosFromBookings(List.of(booking))).thenReturn(List.of(
                    new UnavailableDateRangeDto(
                            booking.getDateFrom(),
                            booking.getDateTo(),
                            "BOOKING",
                            booking.getStatus()
                    )
            ));

            List<UnavailableDateRangeDto> result = availabilityService.getUnavailableRangesByProperty(10L, from, to);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).source()).isEqualTo("BOOKING");
            assertThat(result.get(1).source()).isEqualTo("BLOCK");
        }

        @Test
        void shouldReturnCombinedRangesWithoutFilter_whenDateRangeMissing() {
            AvailabilityBlock block = AvailabilityBlock.builder()
                    .dateFrom(LocalDate.of(2026, 6, 3))
                    .dateTo(LocalDate.of(2026, 6, 4))
                    .build();
            Booking booking = Booking.builder()
                    .dateFrom(LocalDate.of(2026, 6, 7))
                    .dateTo(LocalDate.of(2026, 6, 9))
                    .status(BookingStatus.CREATED)
                    .build();

            when(propertyRepository.existsById(10L)).thenReturn(true);
            when(availabilityRepository.findAllByPropertyId(10L)).thenReturn(List.of(block));
            when(bookingRepository.findAllByPropertyIdAndStatusNotIn(
                    eq(10L), anyList(), any()
            )).thenReturn(new PageImpl<>(List.of(booking)));
            when(mapper.toUnavailableDateRangeDtosFromBlocks(List.of(block))).thenReturn(List.of(
                    new UnavailableDateRangeDto(
                            block.getDateFrom(),
                            block.getDateTo(),
                            "BLOCK",
                            null
                    )
            ));
            when(mapper.toUnavailableDateRangeDtosFromBookings(List.of(booking))).thenReturn(List.of(
                    new UnavailableDateRangeDto(
                            booking.getDateFrom(),
                            booking.getDateTo(),
                            "BOOKING",
                            booking.getStatus()
                    )
            ));

            List<UnavailableDateRangeDto> result = availabilityService.getUnavailableRangesByProperty(10L, null, null);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).source()).isEqualTo("BLOCK");
            assertThat(result.get(1).source()).isEqualTo("BOOKING");
        }
    }

    @Nested
    @DisplayName("deleteBlock()")
    class DeleteBlockTests {

        @Test
        void shouldThrowEntityNotFound_whenBlockMissing() {
            when(availabilityRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> availabilityService.deleteBlock(10L, 99L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Block not found");
        }

        @Test
        void shouldThrowAccessDenied_whenCurrentUserCannotDeleteBlock() {
            AvailabilityBlock block = AvailabilityBlock.builder()
                    .id(99L)
                    .property(property)
                    .build();
            when(availabilityRepository.findById(99L)).thenReturn(Optional.of(block));
            when(authService.getCurrentUser()).thenReturn(otherUser);

            assertThatThrownBy(() -> availabilityService.deleteBlock(10L, 99L))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You do not have permission to delete this block");
            verify(availabilityRepository, never()).delete(any(AvailabilityBlock.class));
        }

        @Test
        void shouldDeleteBlock_whenCurrentHostOwnsProperty() {
            AvailabilityBlock block = AvailabilityBlock.builder()
                    .id(99L)
                    .property(property)
                    .build();
            when(availabilityRepository.findById(99L)).thenReturn(Optional.of(block));
            when(authService.getCurrentUser()).thenReturn(host);

            availabilityService.deleteBlock(10L, 99L);

            verify(availabilityRepository).delete(block);
        }
    }
}
