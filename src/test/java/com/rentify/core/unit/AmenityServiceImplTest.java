package com.rentify.core.unit;

import com.rentify.core.dto.property.AmenityCategoryGroupDto;
import com.rentify.core.dto.property.AmenityDto;
import com.rentify.core.entity.Amenity;
import com.rentify.core.enums.AmenityCategory;
import com.rentify.core.mapper.AmenityMapper;
import com.rentify.core.repository.AmenityRepository;
import com.rentify.core.service.impl.AmenityServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AmenityServiceImplTest {

    @Mock private AmenityRepository amenityRepository;
    @Mock private AmenityMapper amenityMapper;

    @InjectMocks
    private AmenityServiceImpl amenityService;

    @Nested
    @DisplayName("getAmenities()")
    class GetAmenitiesTests {

        @Test
        void shouldReturnAllAmenities_whenCategoryIsNull() {
            List<Amenity> amenities = List.of(Amenity.builder().id(1L).name("Wi-Fi").build());
            List<AmenityDto> dtos = List.of(new AmenityDto(1L, "Wi-Fi", AmenityCategory.BASIC, "wifi", "icon"));
            when(amenityRepository.findAllByOrderByCategoryAscNameAsc()).thenReturn(amenities);
            when(amenityMapper.toDtos(amenities)).thenReturn(dtos);

            List<AmenityDto> result = amenityService.getAmenities(null);

            assertThat(result).containsExactlyElementsOf(dtos);
            verify(amenityRepository).findAllByOrderByCategoryAscNameAsc();
            verify(amenityRepository, never()).findAllByCategoryOrderByNameAsc(AmenityCategory.BASIC);
        }

        @Test
        void shouldReturnAmenitiesByCategory_whenCategoryProvided() {
            List<Amenity> amenities = List.of(Amenity.builder().id(2L).name("Guarded").category(AmenityCategory.VERIFICATION).build());
            List<AmenityDto> dtos = List.of(new AmenityDto(2L, "Guarded", AmenityCategory.VERIFICATION, "guarded", "icon"));
            when(amenityRepository.findAllByCategoryOrderByNameAsc(AmenityCategory.VERIFICATION)).thenReturn(amenities);
            when(amenityMapper.toDtos(amenities)).thenReturn(dtos);

            List<AmenityDto> result = amenityService.getAmenities(AmenityCategory.VERIFICATION);

            assertThat(result).containsExactlyElementsOf(dtos);
            verify(amenityRepository).findAllByCategoryOrderByNameAsc(AmenityCategory.VERIFICATION);
        }
    }

    @Nested
    @DisplayName("getAmenitiesGrouped()")
    class GetAmenitiesGroupedTests {

        @Test
        void shouldGroupAmenitiesByCategoryAndMapNullCategoryToOther() {
            Amenity wifi = Amenity.builder().id(1L).name("Wi-Fi").category(AmenityCategory.BASIC).build();
            Amenity unknown = Amenity.builder().id(2L).name("Unknown").category(null).build();
            AmenityDto wifiDto = new AmenityDto(1L, "Wi-Fi", AmenityCategory.BASIC, "wifi", "icon");
            AmenityDto unknownDto = new AmenityDto(2L, "Unknown", AmenityCategory.OTHER, "unknown", "icon");

            when(amenityRepository.findAllByOrderByCategoryAscNameAsc()).thenReturn(List.of(wifi, unknown));
            when(amenityMapper.toDto(wifi)).thenReturn(wifiDto);
            when(amenityMapper.toDto(unknown)).thenReturn(unknownDto);

            List<AmenityCategoryGroupDto> result = amenityService.getAmenitiesGrouped();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).category()).isEqualTo(AmenityCategory.BASIC);
            assertThat(result.get(0).amenities()).containsExactly(wifiDto);
            assertThat(result.get(1).category()).isEqualTo(AmenityCategory.OTHER);
            assertThat(result.get(1).amenities()).containsExactly(unknownDto);
        }
    }
}
