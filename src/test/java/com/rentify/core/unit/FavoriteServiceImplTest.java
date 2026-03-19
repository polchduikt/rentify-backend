package com.rentify.core.unit;

import com.rentify.core.dto.favorite.FavoriteResponseDto;
import com.rentify.core.entity.Favorite;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.User;
import com.rentify.core.mapper.FavoriteMapper;
import com.rentify.core.repository.FavoriteRepository;
import com.rentify.core.repository.PropertyRepository;
import com.rentify.core.repository.UserRepository;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.impl.FavoriteServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplTest {

    @Mock private FavoriteRepository favoriteRepository;
    @Mock private PropertyRepository propertyRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuthenticationService authenticationService;
    @Mock private FavoriteMapper favoriteMapper;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    private User user;
    private Property property;
    private Favorite favorite;
    private FavoriteResponseDto favoriteDto;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();
        property = Property.builder().id(10L).build();
        favorite = Favorite.builder().id(100L).user(user).property(property).build();
        favoriteDto = new FavoriteResponseDto(100L, 10L, ZonedDateTime.now(), null);
    }

    @Nested
    @DisplayName("addToFavorites()")
    class AddToFavoritesTests {

        @Test
        void shouldAddPropertyToFavorites_whenNotAlreadyAdded() {
            when(authenticationService.getCurrentUser()).thenReturn(user);
            when(favoriteRepository.existsByUser_IdAndProperty_Id(1L, 10L)).thenReturn(false);
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(userRepository.getReferenceById(1L)).thenReturn(user);
            when(favoriteRepository.saveAndFlush(any(Favorite.class))).thenReturn(favorite);
            when(favoriteMapper.toDto(favorite)).thenReturn(favoriteDto);

            FavoriteResponseDto result = favoriteService.addToFavorites(10L);

            assertThat(result.id()).isEqualTo(100L);
            assertThat(result.propertyId()).isEqualTo(10L);
        }

        @Test
        void shouldThrowIllegalArgument_whenPropertyAlreadyInFavorites() {
            when(authenticationService.getCurrentUser()).thenReturn(user);
            when(favoriteRepository.existsByUser_IdAndProperty_Id(1L, 10L)).thenReturn(true);

            assertThatThrownBy(() -> favoriteService.addToFavorites(10L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Property is already in favorites");

            verify(propertyRepository, never()).findById(10L);
        }

        @Test
        void shouldThrowEntityNotFound_whenPropertyNotFound() {
            when(authenticationService.getCurrentUser()).thenReturn(user);
            when(favoriteRepository.existsByUser_IdAndProperty_Id(1L, 10L)).thenReturn(false);
            when(propertyRepository.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> favoriteService.addToFavorites(10L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Property not found");
        }

        @Test
        void shouldThrowIllegalArgument_whenUniqueConstraintViolationOccurs() {
            when(authenticationService.getCurrentUser()).thenReturn(user);
            when(favoriteRepository.existsByUser_IdAndProperty_Id(1L, 10L)).thenReturn(false);
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(userRepository.getReferenceById(1L)).thenReturn(user);
            when(favoriteRepository.saveAndFlush(any(Favorite.class)))
                    .thenThrow(new DataIntegrityViolationException("duplicate"));

            assertThatThrownBy(() -> favoriteService.addToFavorites(10L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Property is already in favorites");
        }
    }

    @Nested
    @DisplayName("removeFromFavorites()")
    class RemoveFromFavoritesTests {

        @Test
        void shouldRemoveFavorite_whenRecordExists() {
            when(authenticationService.getCurrentUser()).thenReturn(user);
            when(favoriteRepository.existsByUser_IdAndProperty_Id(1L, 10L)).thenReturn(true);

            favoriteService.removeFromFavorites(10L);

            verify(favoriteRepository).deleteByUser_IdAndProperty_Id(1L, 10L);
        }

        @Test
        void shouldThrowEntityNotFound_whenFavoriteMissing() {
            when(authenticationService.getCurrentUser()).thenReturn(user);
            when(favoriteRepository.existsByUser_IdAndProperty_Id(1L, 10L)).thenReturn(false);

            assertThatThrownBy(() -> favoriteService.removeFromFavorites(10L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Favorite not found");
        }
    }

    @Nested
    @DisplayName("getMyFavorites()")
    class GetMyFavoritesTests {

        @Test
        void shouldReturnMappedFavoritesOrderedByCreatedAtDesc() {
            Favorite favorite2 = Favorite.builder().id(101L).user(user).property(property).build();
            FavoriteResponseDto favoriteDto2 = new FavoriteResponseDto(101L, 10L, ZonedDateTime.now(), null);

            when(authenticationService.getCurrentUser()).thenReturn(user);
            when(favoriteRepository.findAllByUser_IdOrderByCreatedAtDesc(1L)).thenReturn(List.of(favorite, favorite2));
            when(favoriteMapper.toDto(favorite)).thenReturn(favoriteDto);
            when(favoriteMapper.toDto(favorite2)).thenReturn(favoriteDto2);

            List<FavoriteResponseDto> result = favoriteService.getMyFavorites();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).id()).isEqualTo(100L);
            assertThat(result.get(1).id()).isEqualTo(101L);
        }
    }
}
