package com.rentify.core.unit;

import com.rentify.core.dto.location.LocationSuggestionDto;
import com.rentify.core.entity.City;
import com.rentify.core.entity.District;
import com.rentify.core.enums.LocationSuggestionType;
import com.rentify.core.repository.CityRepository;
import com.rentify.core.repository.DistrictRepository;
import com.rentify.core.repository.MetroStationRepository;
import com.rentify.core.repository.ResidentialComplexRepository;
import com.rentify.core.service.impl.LocationDirectoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationDirectoryServiceImplTest {

    @Mock private CityRepository cityRepository;
    @Mock private DistrictRepository districtRepository;
    @Mock private MetroStationRepository metroStationRepository;
    @Mock private ResidentialComplexRepository residentialComplexRepository;

    @InjectMocks
    private LocationDirectoryServiceImpl locationDirectoryService;

    private City city;

    @BeforeEach
    void setUp() {
        city = City.builder()
                .id(1L)
                .name("Kyiv")
                .normalizedName("kyiv")
                .region("Kyivska")
                .country("Ukraine")
                .build();
    }

    @Nested
    @DisplayName("suggest()")
    class SuggestTests {

        @Test
        void shouldReturnEmptyList_whenQueryBlank() {
            List<LocationSuggestionDto> result = locationDirectoryService.suggest("   ", null, null, null);

            assertThat(result).isEmpty();
            verify(cityRepository, never()).searchByPrefix(any(), any(PageRequest.class));
            verify(districtRepository, never()).searchByPrefix(any(), any(), any(PageRequest.class));
        }

        @Test
        void shouldSearchOnlyRequestedType_withResolvedLimit() {
            District district = District.builder()
                    .id(10L)
                    .name("Shevchenkivskyi")
                    .normalizedName("shevchenkivskyi")
                    .city(city)
                    .build();
            when(districtRepository.searchByPrefix("ky", 1L, PageRequest.of(0, 5)))
                    .thenReturn(List.of(district));

            List<LocationSuggestionDto> result = locationDirectoryService.suggest("  Ky  ", 1L, List.of("district"), 5);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).type()).isEqualTo(LocationSuggestionType.DISTRICT);
            assertThat(result.get(0).cityName()).isEqualTo("Kyiv");
            verify(cityRepository, never()).searchByPrefix(any(), any(PageRequest.class));
            verify(metroStationRepository, never()).searchByPrefix(any(), any(), any(PageRequest.class));
            verify(residentialComplexRepository, never()).searchByPrefix(any(), any(), any(PageRequest.class));
        }

        @Test
        void shouldUseAllTypesAndClampLimit_whenTypesInvalidAndLimitTooLarge() {
            when(cityRepository.searchByPrefix("ky", PageRequest.of(0, 12))).thenReturn(List.of());
            when(districtRepository.searchByPrefix("ky", 1L, PageRequest.of(0, 12))).thenReturn(List.of());
            when(metroStationRepository.searchByPrefix("ky", 1L, PageRequest.of(0, 12))).thenReturn(List.of());
            when(residentialComplexRepository.searchByPrefix("ky", 1L, PageRequest.of(0, 12))).thenReturn(List.of());

            List<LocationSuggestionDto> result = locationDirectoryService.suggest("ky", 1L, List.of("unknown"), 120);

            assertThat(result).isEmpty();
            verify(cityRepository).searchByPrefix("ky", PageRequest.of(0, 12));
            verify(districtRepository).searchByPrefix("ky", 1L, PageRequest.of(0, 12));
            verify(metroStationRepository).searchByPrefix("ky", 1L, PageRequest.of(0, 12));
            verify(residentialComplexRepository).searchByPrefix("ky", 1L, PageRequest.of(0, 12));
        }

        @Test
        void shouldTrimResultToLimit_whenRepositoriesReturnMoreThanRequested() {
            City city2 = City.builder()
                    .id(2L)
                    .name("Kyiv region")
                    .normalizedName("kyiv region")
                    .region("Kyivska")
                    .country("Ukraine")
                    .build();
            City city3 = City.builder()
                    .id(3L)
                    .name("Kyiv city")
                    .normalizedName("kyiv city")
                    .region("Kyivska")
                    .country("Ukraine")
                    .build();
            when(cityRepository.searchByPrefix(eq("ky"), eq(PageRequest.of(0, 2))))
                    .thenReturn(List.of(city, city2, city3));

            List<LocationSuggestionDto> result = locationDirectoryService.suggest("ky", null, List.of("city"), 2);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).type()).isEqualTo(LocationSuggestionType.CITY);
            assertThat(result.get(1).type()).isEqualTo(LocationSuggestionType.CITY);
        }
    }
}
