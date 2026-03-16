package com.rentify.core.repository;

import com.rentify.core.entity.ResidentialComplex;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResidentialComplexRepository extends JpaRepository<ResidentialComplex, Long> {

    @Query("""
            SELECT rc FROM ResidentialComplex rc
            WHERE (:cityId IS NULL OR rc.city.id = :cityId)
              AND (lower(rc.name) LIKE concat(:query, '%')
                OR lower(rc.normalizedName) LIKE concat(:query, '%'))
            ORDER BY rc.name ASC
            """)
    List<ResidentialComplex> searchByPrefix(
            @Param("query") String query,
            @Param("cityId") Long cityId,
            Pageable pageable
    );
}
