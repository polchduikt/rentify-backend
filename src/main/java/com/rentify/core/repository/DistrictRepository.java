package com.rentify.core.repository;

import com.rentify.core.entity.District;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {

    @Query("""
            SELECT d FROM District d
            WHERE (:cityId IS NULL OR d.city.id = :cityId)
              AND (lower(d.name) LIKE concat(:query, '%')
                OR lower(d.normalizedName) LIKE concat(:query, '%'))
            ORDER BY d.name ASC
            """)
    List<District> searchByPrefix(
            @Param("query") String query,
            @Param("cityId") Long cityId,
            Pageable pageable
    );
}
