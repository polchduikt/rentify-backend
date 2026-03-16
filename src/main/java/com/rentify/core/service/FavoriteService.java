package com.rentify.core.service;

import com.rentify.core.dto.favorite.FavoriteResponseDto;

import java.util.List;

public interface FavoriteService {
    FavoriteResponseDto addToFavorites(Long propertyId);
    void removeFromFavorites(Long propertyId);
    List<FavoriteResponseDto> getMyFavorites();
}
