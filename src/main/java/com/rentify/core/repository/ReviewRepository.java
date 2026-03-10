package com.rentify.core.repository;

import com.rentify.core.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findAllByPropertyId(Long propertyId, Pageable pageable);
    boolean existsByBookingId(Long bookingId);
    boolean existsByPropertyId(Long propertyId);
    long countByPropertyId(Long propertyId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.property.id = :propertyId")
    Double findAverageRatingByPropertyId(@Param("propertyId") Long propertyId);
}
