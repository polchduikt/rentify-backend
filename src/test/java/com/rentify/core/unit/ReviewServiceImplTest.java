package com.rentify.core.unit;

import com.rentify.core.dto.review.ReviewDto;
import com.rentify.core.dto.review.ReviewRequestDto;
import com.rentify.core.entity.Booking;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.Review;
import com.rentify.core.entity.User;
import com.rentify.core.enums.BookingStatus;
import com.rentify.core.mapper.ReviewMapper;
import com.rentify.core.repository.BookingRepository;
import com.rentify.core.repository.PropertyRepository;
import com.rentify.core.repository.ReviewRepository;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.impl.ReviewServiceImpl;
import com.rentify.core.validation.ReviewValidator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private PropertyRepository propertyRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private AuthenticationService authService;
    @Mock private ReviewMapper reviewMapper;
    @Mock private ReviewValidator reviewValidator;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User tenant;
    private User otherUser;
    private Property property;
    private Booking booking;
    private ReviewRequestDto request;
    private ReviewDto reviewDto;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        tenant = User.builder().id(1L).firstName("Illia").build();
        otherUser = User.builder().id(2L).firstName("Other").build();

        property = Property.builder()
                .id(10L)
                .reviewCount(0L)
                .averageRating(BigDecimal.ZERO)
                .build();

        booking = Booking.builder()
                .id(20L)
                .tenant(tenant)
                .property(property)
                .status(BookingStatus.COMPLETED)
                .build();

        request = new ReviewRequestDto(10L, 20L, (short) 5, "Great stay");
        reviewDto = new ReviewDto(1L, 10L, 20L, 1L, (short) 5, "Illia", "Great stay", ZonedDateTime.now());
        pageable = PageRequest.of(0, 10);

        lenient().doAnswer(invocation -> {
            Booking bookingArg = invocation.getArgument(0);
            Property propertyArg = invocation.getArgument(1);
            User authorArg = invocation.getArgument(2);
            boolean alreadyReviewed = invocation.getArgument(3);

            if (!bookingArg.getTenant().getId().equals(authorArg.getId())) {
                throw new AccessDeniedException("You can only review bookings that belong to you");
            }
            if (!bookingArg.getProperty().getId().equals(propertyArg.getId())) {
                throw new IllegalArgumentException("Booking does not belong to the specified property");
            }
            if (bookingArg.getStatus() != BookingStatus.COMPLETED) {
                throw new IllegalStateException("You can only review properties after your stay is COMPLETED");
            }
            if (alreadyReviewed) {
                throw new IllegalStateException("You have already reviewed this booking");
            }
            return null;
        }).when(reviewValidator).validateReviewEligibility(any(Booking.class), any(Property.class), any(User.class), anyBoolean());
    }

    @Nested
    @DisplayName("createReview()")
    class CreateReviewTests {

        @Test
        void shouldCreateReviewAndRecalculatePropertyRating_whenRequestIsValid() {
            Review savedReview = Review.builder()
                    .id(1L)
                    .booking(booking)
                    .property(property)
                    .author(tenant)
                    .rating((short) 5)
                    .comment("Great stay")
                    .build();

            when(authService.getCurrentUser()).thenReturn(tenant);
            when(bookingRepository.findById(20L)).thenReturn(Optional.of(booking));
            when(propertyRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(property));
            when(reviewRepository.existsByBookingId(20L)).thenReturn(false);
            when(reviewRepository.save(org.mockito.ArgumentMatchers.any(Review.class))).thenReturn(savedReview);
            when(reviewRepository.countByPropertyId(10L)).thenReturn(3L);
            when(reviewRepository.findAverageRatingByPropertyId(10L)).thenReturn(new BigDecimal("4.666"));
            when(reviewMapper.toDto(savedReview)).thenReturn(reviewDto);

            ReviewDto result = reviewService.createReview(request);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(property.getReviewCount()).isEqualTo(3L);
            assertThat(property.getAverageRating()).isEqualByComparingTo("4.67");
            verify(propertyRepository).save(property);
        }

        @Test
        void shouldThrowEntityNotFound_whenBookingIsMissing() {
            when(authService.getCurrentUser()).thenReturn(tenant);
            when(bookingRepository.findById(20L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.createReview(request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Booking not found");
        }

        @Test
        void shouldThrowEntityNotFound_whenPropertyIsMissing() {
            when(authService.getCurrentUser()).thenReturn(tenant);
            when(bookingRepository.findById(20L)).thenReturn(Optional.of(booking));
            when(propertyRepository.findByIdForUpdate(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.createReview(request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Property not found");
        }

        @Test
        void shouldThrowAccessDenied_whenBookingBelongsToAnotherTenant() {
            booking.setTenant(otherUser);
            when(authService.getCurrentUser()).thenReturn(tenant);
            when(bookingRepository.findById(20L)).thenReturn(Optional.of(booking));
            when(propertyRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(property));

            assertThatThrownBy(() -> reviewService.createReview(request))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You can only review bookings that belong to you");
        }

        @Test
        void shouldThrowIllegalArgument_whenBookingAndPropertyDoNotMatch() {
            Property otherProperty = Property.builder().id(11L).build();
            booking.setProperty(otherProperty);
            when(authService.getCurrentUser()).thenReturn(tenant);
            when(bookingRepository.findById(20L)).thenReturn(Optional.of(booking));
            when(propertyRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(property));

            assertThatThrownBy(() -> reviewService.createReview(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Booking does not belong to the specified property");
        }

        @Test
        void shouldThrowIllegalState_whenBookingIsNotCompleted() {
            booking.setStatus(BookingStatus.CONFIRMED);
            when(authService.getCurrentUser()).thenReturn(tenant);
            when(bookingRepository.findById(20L)).thenReturn(Optional.of(booking));
            when(propertyRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(property));

            assertThatThrownBy(() -> reviewService.createReview(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("You can only review properties after your stay is COMPLETED");
        }

        @Test
        void shouldThrowIllegalState_whenBookingIsAlreadyReviewed() {
            when(authService.getCurrentUser()).thenReturn(tenant);
            when(bookingRepository.findById(20L)).thenReturn(Optional.of(booking));
            when(propertyRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(property));
            when(reviewRepository.existsByBookingId(20L)).thenReturn(true);

            assertThatThrownBy(() -> reviewService.createReview(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("You have already reviewed this booking");
        }
    }

    @Nested
    @DisplayName("getPropertyReviews()")
    class GetPropertyReviewsTests {

        @Test
        void shouldReturnMappedPage() {
            Review review = Review.builder().id(1L).property(property).booking(booking).author(tenant).build();
            Page<Review> reviewPage = new PageImpl<>(List.of(review), pageable, 1);
            when(reviewRepository.findAllByPropertyId(10L, pageable)).thenReturn(reviewPage);
            when(reviewMapper.toDto(review)).thenReturn(reviewDto);

            Page<ReviewDto> result = reviewService.getPropertyReviews(10L, pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).id()).isEqualTo(1L);
            verify(reviewRepository).findAllByPropertyId(10L, pageable);
        }
    }
}
