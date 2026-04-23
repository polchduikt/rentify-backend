package com.rentify.core.repository;

import com.rentify.core.entity.City;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    @Query("""
            SELECT c FROM City c
            WHERE lower(c.name) LIKE concat(:query, '%') ESCAPE '\\'
               OR lower(c.normalizedName) LIKE concat(:query, '%') ESCAPE '\\'
            ORDER BY c.name ASC
            """)
    List<City> searchByPrefix(@Param("query") String query, Pageable pageable);

    Optional<City> findFirstByNameIgnoreCaseAndRegionIgnoreCase(String name, String region);

    Optional<City> findFirstByNameIgnoreCaseAndCountryIgnoreCase(String name, String country);
}
