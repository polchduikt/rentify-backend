package com.rentify.core.repository;

import com.rentify.core.entity.PropertyPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyPhotoRepository extends JpaRepository<PropertyPhoto, Long> {
    List<PropertyPhoto> findAllByPropertyIdOrderBySortOrderAsc(Long propertyId);
}