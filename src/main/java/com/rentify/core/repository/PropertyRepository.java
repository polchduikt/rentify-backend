package com.rentify.core.repository;

import com.rentify.core.entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long>, JpaSpecificationExecutor<Property> {
    Page<Property> findAllByHostId(Long hostId, Pageable pageable);

    @Modifying
    @Query("UPDATE Property p SET p.isTopPromoted = false, p.topPromotedUntil = null " +
            "WHERE p.isTopPromoted = true " +
            "AND p.topPromotedUntil IS NOT NULL " +
            "AND p.topPromotedUntil < :now")
    int deactivateExpiredTopPromotions(@Param("now") ZonedDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Property p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    int incrementViewCount(@Param("id") Long id);
}
