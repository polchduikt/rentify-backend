package com.rentify.core.repository;

import com.rentify.core.entity.Amenity;
import com.rentify.core.enums.AmenityCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    Optional<Amenity> findByName(String name);
    Optional<Amenity> findByNameIgnoreCase(String name);
    Optional<Amenity> findBySlugIgnoreCase(String slug);
    List<Amenity> findAllByOrderByCategoryAscNameAsc();
    List<Amenity> findAllByCategoryOrderByNameAsc(AmenityCategory category);

    @Query("SELECT a FROM Amenity a WHERE lower(a.slug) IN :slugs")
    List<Amenity> findAllBySlugInIgnoreCase(@Param("slugs") List<String> slugs);
}
