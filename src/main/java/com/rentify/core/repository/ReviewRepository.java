package com.rentify.core.repository;

import com.rentify.core.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByPropertyId(Long propertyId);
    List<Review> findAllByAuthorId(Long authorId);
}