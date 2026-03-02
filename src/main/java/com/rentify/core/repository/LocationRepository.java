package com.rentify.core.repository;

import com.rentify.core.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByCityAndRegionAndCountry(String city, String region, String country);
    List<Location> findAllByCity(String city);
}
