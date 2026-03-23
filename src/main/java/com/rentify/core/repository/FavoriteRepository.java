package com.rentify.core.repository;

import com.rentify.core.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByUser_IdAndProperty_Id(Long userId, Long propertyId);
    Optional<Favorite> findByUser_IdAndProperty_Id(Long userId, Long propertyId);
    void deleteByUser_IdAndProperty_Id(Long userId, Long propertyId);
    void deleteByProperty_Id(Long propertyId);
    List<Favorite> findAllByUser_IdOrderByCreatedAtDesc(Long userId);
}
