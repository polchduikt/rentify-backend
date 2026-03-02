package com.rentify.core.repository;

import com.rentify.core.entity.AvailabilityBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AvailabilityBlockRepository extends JpaRepository<AvailabilityBlock, Long> {
    List<AvailabilityBlock> findAllByPropertyId(Long propertyId);
    List<AvailabilityBlock> findAllByPropertyIdAndDateFromLessThanEqualAndDateToGreaterThanEqual(
            Long propertyId, LocalDate dateTo, LocalDate dateFrom);
}