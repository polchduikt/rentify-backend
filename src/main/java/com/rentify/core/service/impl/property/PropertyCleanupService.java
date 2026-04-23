package com.rentify.core.service.impl.property;

import com.rentify.core.repository.AvailabilityBlockRepository;
import com.rentify.core.repository.BookingRepository;
import com.rentify.core.repository.ConversationRepository;
import com.rentify.core.repository.FavoriteRepository;
import com.rentify.core.repository.ReviewRepository;
import com.rentify.core.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PropertyCleanupService {

    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final ConversationRepository conversationRepository;
    private final FavoriteRepository favoriteRepository;
    private final AvailabilityBlockRepository availabilityBlockRepository;

    public void cleanupBeforeDelete(Long propertyId) {
        if (bookingRepository.existsByPropertyId(propertyId)) {
            throw DomainException.conflict("PROPERTY_DELETE_FORBIDDEN", "Property cannot be deleted because it has bookings");
        }
        if (reviewRepository.existsByPropertyId(propertyId)) {
            throw DomainException.conflict("PROPERTY_DELETE_FORBIDDEN", "Property cannot be deleted because it has reviews");
        }
        if (conversationRepository.existsByPropertyId(propertyId)) {
            throw DomainException.conflict("PROPERTY_DELETE_FORBIDDEN", "Property cannot be deleted because it has conversations");
        }
        favoriteRepository.deleteByProperty_Id(propertyId);
        availabilityBlockRepository.deleteAllByPropertyId(propertyId);
    }
}
