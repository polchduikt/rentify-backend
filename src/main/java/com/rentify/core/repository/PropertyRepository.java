package com.rentify.core.repository;

import com.rentify.core.entity.Property;
import com.rentify.core.enums.PropertyStatus;
import com.rentify.core.enums.RentalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findAllByStatusAndRentalType(PropertyStatus status, RentalType rentalType);
    List<Property> findAllByHostId(Long hostId);
}
