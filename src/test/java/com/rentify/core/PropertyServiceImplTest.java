package com.rentify.core;

import com.rentify.core.dto.property.PropertyCreateRequestDto;
import com.rentify.core.dto.property.PropertyPhotoDto;
import com.rentify.core.dto.property.PropertyResponseDto;
import com.rentify.core.dto.property.PropertySearchCriteriaDto;
import com.rentify.core.entity.Amenity;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.PropertyPhoto;
import com.rentify.core.entity.PropertyPricing;
import com.rentify.core.entity.PropertyRule;
import com.rentify.core.entity.Role;
import com.rentify.core.entity.User;
import com.rentify.core.enums.PropertyStatus;
import com.rentify.core.mapper.PropertyMapper;
import com.rentify.core.repository.*;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.CloudinaryService;
import com.rentify.core.service.impl.PropertyServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceImplTest {

    @Mock private PropertyRepository       propertyRepository;
    @Mock private AmenityRepository        amenityRepository;
    @Mock private PropertyMapper           propertyMapper;
    @Mock private AuthenticationService    authenticationService;
    @Mock private CloudinaryService        cloudinaryService;
    @Mock private PropertyPhotoRepository  propertyPhotoRepository;

    @InjectMocks
    private PropertyServiceImpl propertyService;
    private User hostUser;
    private User otherUser;
    private User adminUser;
    private Property property;
    private PropertyResponseDto responseDto;
    private Pageable pageable;

//    @BeforeEach
//    void setUp() {
//        Role hostRole = new Role();
//        hostRole.setName("ROLE_HOST");
//        hostUser = new User();
//        hostUser.setId(1L);
//        hostUser.setEmail("host@rentify.com");
//        hostUser.setRoles(Set.of(hostRole));
//        Role tenantRole = new Role();
//        tenantRole.setName("ROLE_TENANT");
//        otherUser = new User();
//        otherUser.setId(2L);
//        otherUser.setEmail("other@rentify.com");
//        otherUser.setRoles(Set.of(tenantRole));
//        Role adminRole = new Role();
//        adminRole.setName("ROLE_ADMIN");
//        adminUser = new User();
//        adminUser.setId(3L);
//        adminUser.setEmail("admin@rentify.com");
//        adminUser.setRoles(Set.of(adminRole));
//        property = new Property();
//        property.setId(10L);
//        property.setTitle("Тестова квартира");
//        property.setHost(hostUser);
//        property.setStatus(PropertyStatus.ACTIVE);
//        responseDto = new PropertyResponseDto(
//                10L, 1L, null, "Тестова квартира", "Опис", null,
//                PropertyStatus.ACTIVE, "apartment", (short)2, (short)5, (short)10,
//                java.math.BigDecimal.valueOf(60.0), (short)4, null, null,
//                null, null, List.of(), Set.of(), java.time.ZonedDateTime.now(), java.time.ZonedDateTime.now()
//        );
//        pageable = PageRequest.of(0, 10, Sort.by("id"));
//    }

    @Nested
    class GetAllPropertiesTests {

        @Test
        void shouldReturnPageOfDtos_whenPropertiesExist() {
            Page<Property> entityPage = new PageImpl<>(List.of(property), pageable, 1);
            when(propertyRepository.findAll(pageable)).thenReturn(entityPage);
            when(propertyMapper.toDto(property)).thenReturn(responseDto);
            Page<PropertyResponseDto> result = propertyService.getAllProperties(pageable);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).id()).isEqualTo(10L);
            verify(propertyRepository).findAll(pageable);
            verify(propertyMapper).toDto(property);
        }

        @Test
        void shouldReturnEmptyPage_whenNoProperties() {
            Page<Property> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            when(propertyRepository.findAll(pageable)).thenReturn(emptyPage);
            Page<PropertyResponseDto> result = propertyService.getAllProperties(pageable);
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("getPropertyById()")
    class GetPropertyByIdTests {

        @Test
        void shouldReturnDto_whenPropertyExists() {
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(propertyMapper.toDto(property)).thenReturn(responseDto);
            PropertyResponseDto result = propertyService.getPropertyById(10L);
            assertThat(result.id()).isEqualTo(10L);
            assertThat(result.title()).isEqualTo("Тестова квартира");
        }

        @Test
        void shouldThrowEntityNotFound_whenPropertyDoesNotExist() {
            when(propertyRepository.findById(999L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> propertyService.getPropertyById(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Property not found");
        }
    }

    @Nested
    @DisplayName("create()")
    class CreatePropertyTests {

        @Test
        void shouldCreateProperty_withoutAmenitiesAndAddress() {
            PropertyCreateRequestDto createRequest = mock(PropertyCreateRequestDto.class);
            when(createRequest.amenityIds()).thenReturn(null);
            Property newProperty = new Property();
            newProperty.setId(20L);
            when(authenticationService.getCurrentUser()).thenReturn(hostUser);
            when(propertyMapper.toEntity(createRequest)).thenReturn(newProperty);
            when(propertyRepository.save(newProperty)).thenReturn(newProperty);
            when(propertyMapper.toDto(newProperty)).thenReturn(responseDto);
            PropertyResponseDto result = propertyService.create(createRequest);
            assertThat(result).isNotNull();
            verify(propertyRepository).save(newProperty);
            assertThat(newProperty.getHost()).isEqualTo(hostUser);
            assertThat(newProperty.getStatus()).isEqualTo(PropertyStatus.DRAFT);
        }

        @Test
        void shouldSetAmenities_whenAmenityIdsProvided() {
            PropertyCreateRequestDto createRequest = mock(PropertyCreateRequestDto.class);
            when(createRequest.amenityIds()).thenReturn(List.of(1L, 2L));
            Amenity amenity1 = new Amenity(); amenity1.setId(1L);
            Amenity amenity2 = new Amenity(); amenity2.setId(2L);
            Property newProperty = new Property();
            newProperty.setId(20L);
            when(authenticationService.getCurrentUser()).thenReturn(hostUser);
            when(propertyMapper.toEntity(createRequest)).thenReturn(newProperty);
            when(amenityRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(amenity1, amenity2));
            when(propertyRepository.save(newProperty)).thenReturn(newProperty);
            when(propertyMapper.toDto(newProperty)).thenReturn(responseDto);
            propertyService.create(createRequest);
            assertThat(newProperty.getAmenities()).containsExactlyInAnyOrder(amenity1, amenity2);
        }

        @Test
        void shouldLinkPricingToProperty_whenPricingPresent() {
            PropertyCreateRequestDto createRequest = mock(PropertyCreateRequestDto.class);
            when(createRequest.amenityIds()).thenReturn(null);
            PropertyPricing pricing = new PropertyPricing();
            Property newProperty = new Property();
            newProperty.setPricing(pricing);
            when(authenticationService.getCurrentUser()).thenReturn(hostUser);
            when(propertyMapper.toEntity(createRequest)).thenReturn(newProperty);
            when(propertyRepository.save(newProperty)).thenReturn(newProperty);
            when(propertyMapper.toDto(newProperty)).thenReturn(responseDto);
            propertyService.create(createRequest);
            assertThat(pricing.getProperty()).isEqualTo(newProperty);
        }

        @Test
        void shouldLinkRulesToProperty_whenRulesPresent() {
            PropertyCreateRequestDto createRequest = mock(PropertyCreateRequestDto.class);
            when(createRequest.amenityIds()).thenReturn(null);
            PropertyRule rules = new PropertyRule();
            Property newProperty = new Property();
            newProperty.setRules(rules);
            when(authenticationService.getCurrentUser()).thenReturn(hostUser);
            when(propertyMapper.toEntity(createRequest)).thenReturn(newProperty);
            when(propertyRepository.save(newProperty)).thenReturn(newProperty);
            when(propertyMapper.toDto(newProperty)).thenReturn(responseDto);
            propertyService.create(createRequest);
            assertThat(rules.getProperty()).isEqualTo(newProperty);
        }
    }

    @Nested
    @DisplayName("uploadPhoto()")
    class UploadPhotoTests {

        @Mock private MultipartFile mockFile;

        @Test
        void shouldUploadPhoto_whenUserIsOwner() {
            String imageUrl = "https://cloudinary.com/test/photo.jpg";
            PropertyPhoto savedPhoto = PropertyPhoto.builder()
                    .id(1L)
                    .property(property)
                    .url(imageUrl)
                    .sortOrder(0)
                    .build();
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(authenticationService.getCurrentUser()).thenReturn(hostUser);
            when(cloudinaryService.uploadFile(mockFile)).thenReturn(imageUrl);
            when(propertyPhotoRepository.save(any(PropertyPhoto.class))).thenReturn(savedPhoto);
            PropertyPhotoDto result = propertyService.uploadPhoto(10L, mockFile);
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.url()).isEqualTo(imageUrl);
            assertThat(result.sortOrder()).isEqualTo(0);
            verify(cloudinaryService).uploadFile(mockFile);
        }

        @Test
        void shouldThrowAccessDenied_whenUserIsNotOwner() {
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(authenticationService.getCurrentUser()).thenReturn(otherUser);
            assertThatThrownBy(() -> propertyService.uploadPhoto(10L, mockFile))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("not the owner");
            verify(cloudinaryService, never()).uploadFile(any());
        }
    }

    @Nested
    @DisplayName("changePropertyStatus()")
    class ChangePropertyStatusTests {

        @Test
        void shouldDeactivate_whenCalledByHost() {
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(authenticationService.getCurrentUser()).thenReturn(hostUser);
            when(propertyRepository.save(property)).thenReturn(property);
            when(propertyMapper.toDto(property)).thenReturn(responseDto);
            propertyService.changePropertyStatus(10L, PropertyStatus.INACTIVE);
            assertThat(property.getStatus()).isEqualTo(PropertyStatus.INACTIVE);
            verify(propertyRepository).save(property);
        }

        @Test
        void shouldThrowAccessDenied_whenHostTriesToBlock() {
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(authenticationService.getCurrentUser()).thenReturn(hostUser);
            assertThatThrownBy(() ->
                    propertyService.changePropertyStatus(10L, PropertyStatus.BLOCKED))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("administrators");
        }
    }

//    @Nested
//    @DisplayName("search()")
//    class SearchTests {
//
//        @Test
//        void shouldReturnFilteredPage_whenCriteriaProvided() {
//            PropertySearchCriteriaDto criteria = new PropertySearchCriteriaDto(
//                    "Kyiv", null, null, null, null, null, null, null, null, null, null, null
//            );
//            Page<Property> entityPage = new PageImpl<>(List.of(property), pageable, 1);
//            when(propertyRepository.findAll(any(Specification.class), eq(pageable)))
//                    .thenReturn(entityPage);
//            when(propertyMapper.toDto(property)).thenReturn(responseDto);
//            Page<PropertyResponseDto> result = propertyService.search(criteria, pageable);
//            assertThat(result.getTotalElements()).isEqualTo(1);
//            assertThat(result.getContent().get(0).id()).isEqualTo(10L);
//            verify(propertyRepository).findAll(any(Specification.class), eq(pageable));
//        }
//    }
}
