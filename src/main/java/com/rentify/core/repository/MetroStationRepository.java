package com.rentify.core.repository;

import com.rentify.core.entity.MetroStation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetroStationRepository extends JpaRepository<MetroStation, Long> {

    @Query("""
            SELECT m FROM MetroStation m
            WHERE (:cityId IS NULL OR m.city.id = :cityId)
              AND (lower(m.name) LIKE concat(:query, '%')
                OR lower(m.normalizedName) LIKE concat(:query, '%'))
            ORDER BY m.name ASC
            """)
    List<MetroStation> searchByPrefix(
            @Param("query") String query,
            @Param("cityId") Long cityId,
            Pageable pageable
    );
}
