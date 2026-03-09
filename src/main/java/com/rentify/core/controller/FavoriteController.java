package com.rentify.core.controller;

import com.rentify.core.dto.favorite.FavoriteResponseDto;
import com.rentify.core.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{propertyId}")
    public ResponseEntity<FavoriteResponseDto> addToFavorites(@PathVariable Long propertyId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(favoriteService.addToFavorites(propertyId));
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<Void> removeFromFavorites(@PathVariable Long propertyId) {
        favoriteService.removeFromFavorites(propertyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<FavoriteResponseDto>> getMyFavorites() {
        return ResponseEntity.ok(favoriteService.getMyFavorites());
    }
}
