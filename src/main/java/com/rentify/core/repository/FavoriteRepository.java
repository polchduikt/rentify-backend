package com.rentify.core.repository;

import com.rentify.core.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByUser_IdAndProperty_Id(Long userId, Long propertyId);
    void deleteByUser_IdAndProperty_Id(Long userId, Long propertyId);
    void deleteByProperty_Id(Long propertyId);
    List<Favorite> findAllByUser_IdOrderByCreatedAtDesc(Long userId);
}
