package com.rentify.core.repository;

import com.rentify.core.entity.Booking;
import com.rentify.core.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findAllByTenantId(Long tenantId, Pageable pageable);
    Page<Booking> findAllByPropertyHostId(Long hostId, Pageable pageable);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.property.id = :propertyId " +
            "AND b.status NOT IN :excludedStatuses " +
            "AND b.dateFrom < :endDate " +
            "AND b.dateTo > :startDate")

    boolean hasOverlappingBookings(@Param("propertyId") Long propertyId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate,
                                   @Param("excludedStatuses") List<BookingStatus> excludedStatuses);

    boolean existsByTenantIdAndPropertyIdAndStatusAndDateToBefore(
            Long tenantId,
            Long propertyId,
            BookingStatus status,
            LocalDate dateTo
    );

    @Modifying
    @Query("UPDATE Booking b SET b.status = :newStatus WHERE b.status = :oldStatus AND b.dateFrom <= :currentDate")
    int updateStatusToInProgress(
            @Param("oldStatus") BookingStatus oldStatus,
            @Param("newStatus") BookingStatus newStatus,
            @Param("currentDate") LocalDate currentDate);

    @Modifying
    @Query("UPDATE Booking b SET b.status = :newStatus WHERE b.status IN :oldStatuses AND b.dateTo < :currentDate")
    int updateStatusToCompleted(
            @Param("oldStatuses") List<BookingStatus> oldStatuses,
            @Param("newStatus") BookingStatus newStatus,
            @Param("currentDate") LocalDate currentDate);

    boolean existsByTenantIdAndPropertyIdAndStatus(Long tenantId, Long propertyId, BookingStatus status);
    boolean existsByPropertyId(Long propertyId);

    List<Booking> findAllByPropertyIdAndStatusNotIn(Long propertyId, List<BookingStatus> excludedStatuses);

    List<Booking> findAllByPropertyIdAndStatusNotInAndDateFromLessThanAndDateToGreaterThan(
            Long propertyId,
            List<BookingStatus> excludedStatuses,
            LocalDate endDate,
            LocalDate startDate
    );
}
