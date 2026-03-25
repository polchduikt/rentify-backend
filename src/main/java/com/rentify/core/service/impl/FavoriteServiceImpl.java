package com.rentify.core.service.impl;

import com.rentify.core.dto.favorite.FavoriteResponseDto;
import com.rentify.core.entity.Favorite;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.User;
import com.rentify.core.mapper.FavoriteMapper;
import com.rentify.core.repository.FavoriteRepository;
import com.rentify.core.repository.PropertyRepository;
import com.rentify.core.repository.UserRepository;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.FavoriteService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final FavoriteMapper favoriteMapper;

    @Override
    @Transactional
    public FavoriteResponseDto addToFavorites(Long propertyId) {
        Long userId = authenticationService.getCurrentUser().getId();
        Favorite existingFavorite = favoriteRepository.findByUser_IdAndProperty_Id(userId, propertyId).orElse(null);
        if (existingFavorite != null) {
            return favoriteMapper.toDto(existingFavorite);
        }
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));
        User userReference = userRepository.getReferenceById(userId);
        Favorite favorite = Favorite.builder()
                .user(userReference)
                .property(property)
                .build();
        Favorite savedFavorite;
        try {
            savedFavorite = favoriteRepository.saveAndFlush(favorite);
        } catch (DataIntegrityViolationException ex) {
            Favorite favoriteAfterRace = favoriteRepository.findByUser_IdAndProperty_Id(userId, propertyId)
                    .orElseThrow(() -> new IllegalStateException("Failed to create favorite entry"));
            return favoriteMapper.toDto(favoriteAfterRace);
        }
        return favoriteMapper.toDto(savedFavorite);
    }

    @Override
    @Transactional
    public void removeFromFavorites(Long propertyId) {
        Long userId = authenticationService.getCurrentUser().getId();
        if (!favoriteRepository.existsByUser_IdAndProperty_Id(userId, propertyId)) {
            throw new EntityNotFoundException("Favorite not found");
        }
        favoriteRepository.deleteByUser_IdAndProperty_Id(userId, propertyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteResponseDto> getMyFavorites() {
        Long userId = authenticationService.getCurrentUser().getId();
        return favoriteMapper.toDtos(favoriteRepository.findAllByUser_IdOrderByCreatedAtDesc(userId));
    }
}
