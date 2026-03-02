package com.rentify.core.repository;

import com.rentify.core.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByTenantId(Long tenantId);
    List<Booking> findAllByPropertyId(Long propertyId);
}