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
            "AND b.dateFrom < :dateTo " +
            "AND b.dateTo > :dateFrom")

    boolean hasOverlappingBookings(@Param("propertyId") Long propertyId,
                                   @Param("dateFrom") LocalDate dateFrom,
                                   @Param("dateTo") LocalDate dateTo,
                                   @Param("excludedStatuses") List<BookingStatus> excludedStatuses);

    @Modifying
    @Query("UPDATE Booking b SET b.status = :newStatus WHERE b.status = :oldStatus AND b.dateFrom <= :currentDate")
    int updateStatusToInProgress(
            @Param("oldStatus") BookingStatus oldStatus,
            @Param("newStatus") BookingStatus newStatus,
            @Param("currentDate") LocalDate currentDate);

    @Modifying
    @Query("UPDATE Booking b SET b.status = :newStatus WHERE b.status IN :oldStatuses AND b.dateTo <= :currentDate")
    int updateStatusToCompleted(
            @Param("oldStatuses") List<BookingStatus> oldStatuses,
            @Param("newStatus") BookingStatus newStatus,
            @Param("currentDate") LocalDate currentDate);

    @Modifying
    @Query(value = "UPDATE bookings SET version = 0 WHERE version IS NULL", nativeQuery = true)
    int normalizeNullVersions();

    boolean existsByPropertyId(Long propertyId);

    Page<Booking> findAllByPropertyIdAndStatusNotIn(
            Long propertyId,
            List<BookingStatus> excludedStatuses,
            Pageable pageable
    );

    @Query("SELECT b FROM Booking b " +
            "WHERE b.property.id = :propertyId " +
            "AND b.status NOT IN :excludedStatuses " +
            "AND b.dateFrom < :dateTo " +
            "AND b.dateTo > :dateFrom")
    Page<Booking> findOverlappingByPropertyIdAndStatusNotIn(
            @Param("propertyId") Long propertyId,
            @Param("excludedStatuses") List<BookingStatus> excludedStatuses,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            Pageable pageable
    );
}
