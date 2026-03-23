package com.rentify.core.repository;

import com.rentify.core.entity.PropertyPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyPhotoRepository extends JpaRepository<PropertyPhoto, Long> {
    Optional<PropertyPhoto> findByIdAndPropertyId(Long id, Long propertyId);
}