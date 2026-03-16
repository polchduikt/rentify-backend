package com.rentify.core.unit;

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
import com.rentify.core.repository.AmenityRepository;
import com.rentify.core.repository.AvailabilityBlockRepository;
import com.rentify.core.repository.BookingRepository;
import com.rentify.core.repository.ConversationRepository;
import com.rentify.core.repository.FavoriteRepository;
import com.rentify.core.repository.PropertyPhotoRepository;
import com.rentify.core.repository.PropertyRepository;
import com.rentify.core.repository.ReviewRepository;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.CloudinaryService;
import com.rentify.core.service.impl.PropertyServiceImpl;
import com.rentify.core.validation.PropertyValidator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PropertyServiceImpl unit tests")
class PropertyServiceImplTest {

    @Mock private PropertyRepository propertyRepository;
    @Mock private AmenityRepository amenityRepository;
    @Mock private PropertyMapper propertyMapper;
    @Mock private AuthenticationService authenticationService;
    @Mock private CloudinaryService cloudinaryService;
    @Mock private PropertyPhotoRepository propertyPhotoRepository;
    @Mock private AvailabilityBlockRepository availabilityBlockRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private ConversationRepository conversationRepository;
    @Mock private FavoriteRepository favoriteRepository;
    @Mock private PropertyValidator propertyValidator;

    @InjectMocks
    private PropertyServiceImpl propertyService;

    private User hostUser;
    private User otherUser;
    private User adminUser;
    private Property property;
    private PropertyResponseDto responseDto;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        Role hostRole = new Role();
        hostRole.setName("ROLE_HOST");
        hostUser = new User();
        hostUser.setId(1L);
        hostUser.setEmail("host@rentify.com");
        hostUser.setRoles(Set.of(hostRole));

        Role tenantRole = new Role();
        tenantRole.setName("ROLE_TENANT");
        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@rentify.com");
        otherUser.setRoles(Set.of(tenantRole));

        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        adminUser = new User();
        adminUser.setId(3L);
        adminUser.setEmail("admin@rentify.com");
        adminUser.setRoles(Set.of(adminRole));

        property = new Property();
        property.setId(10L);
        property.setTitle("Test apartment");
        property.setHost(hostUser);
        property.setStatus(PropertyStatus.ACTIVE);

        responseDto = new PropertyResponseDto(
                10L,
                1L,
                null,
                "Test apartment",
                "Test description",
                null,
                PropertyStatus.ACTIVE,
                "apartment",
                null,
                null,
                null,
                null,
                false,
                0L,
                0L,
                BigDecimal.ZERO,
                null,
                (short) 2,
                (short) 5,
                (short) 10,
                BigDecimal.valueOf(60.0),
                (short) 4,
                null,
                null,
                null,
                null,
                List.of(),
                Set.of(),
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

        pageable = PageRequest.of(0, 10, Sort.by("id"));
    }

    @Nested
    @DisplayName("getAllProperties()")
    class GetAllPropertiesTests {

        @Test
        void shouldReturnPageOfDtosAndApplyTopPrioritySort_whenPropertiesExist() {
            Page<Property> entityPage = new PageImpl<>(List.of(property), pageable, 1);
            when(propertyRepository.findAll(any(Pageable.class))).thenReturn(entityPage);
            when(propertyMapper.toDto(property)).thenReturn(responseDto);

            Page<PropertyResponseDto> result = propertyService.getAllProperties(pageable);
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).id()).isEqualTo(10L);
            verify(propertyRepository).findAll(pageableCaptor.capture());
            verify(propertyMapper).toDto(property);
            assertTopPrioritySort(pageableCaptor.getValue());
            assertThat(pageableCaptor.getValue().getSort().getOrderFor("id")).isNotNull();
        }

        @Test
        void shouldReturnEmptyPage_whenNoProperties() {
            Page<Property> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            when(propertyRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            Page<PropertyResponseDto> result = propertyService.getAllProperties(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            verify(propertyMapper, never()).toDto(any());
        }
    }

    @Nested
    @DisplayName("getPropertyById()")
    class GetPropertyByIdTests {

        @Test
        void shouldReturnDto_whenPropertyExists() {
            when(propertyRepository.incrementViewCount(10L)).thenReturn(1);
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(propertyMapper.toDto(property)).thenReturn(responseDto);

            PropertyResponseDto result = propertyService.getPropertyById(10L);

            assertThat(result.id()).isEqualTo(10L);
            assertThat(result.title()).isEqualTo("Test apartment");
            verify(propertyRepository).incrementViewCount(10L);
        }

        @Test
        void shouldThrowEntityNotFound_whenPropertyDoesNotExist() {
            when(propertyRepository.incrementViewCount(999L)).thenReturn(0);

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
            PropertyCreateRequestDto createRequest = requestWithAmenities(null);
            Property newProperty = new Property();
            newProperty.setId(20L);

            when(authenticationService.getCurrentUser()).thenReturn(hostUser);
            when(propertyMapper.toEntity(createRequest)).thenReturn(newProperty);
            when(propertyRepository.save(newProperty)).thenReturn(newProperty);
            when(propertyMapper.toDto(newProperty)).thenReturn(responseDto);

            PropertyResponseDto result = propertyService.create(createRequest);

            assertThat(result).isNotNull();
            assertThat(newProperty.getHost()).isEqualTo(hostUser);
            assertThat(newProperty.getStatus()).isEqualTo(PropertyStatus.DRAFT);
            verify(propertyRepository).save(newProperty);
        }

        @Test
        void shouldSetAmenities_whenAmenityIdsProvided() {
            PropertyCreateRequestDto createRequest = requestWithAmenities(List.of(1L, 2L));
            Amenity amenity1 = new Amenity();
            amenity1.setId(1L);
            Amenity amenity2 = new Amenity();
            amenity2.setId(2L);
            Property newProperty = new Property();
            newProperty.setId(20L);
            newProperty.setAmenities(null);

            when(authenticationService.getCurrentUser()).thenReturn(hostUser);
            when(propertyMapper.toEntity(createRequest)).thenReturn(newProperty);
            when(amenityRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(amenity1, amenity2));
            when(propertyRepository.save(newProperty)).thenReturn(newProperty);
            when(propertyMapper.toDto(newProperty)).thenReturn(responseDto);

            propertyService.create(createRequest);

            assertThat(newProperty.getAmenities()).isNotNull();
            assertThat(newProperty.getAmenities()).containsExactlyInAnyOrder(amenity1, amenity2);
        }

        @Test
        void shouldLinkPricingToProperty_whenPricingPresent() {
            PropertyCreateRequestDto createRequest = requestWithAmenities(null);
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
            PropertyCreateRequestDto createRequest = requestWithAmenities(null);
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
        void shouldUploadPhoto_whenUserCanManageProperty() {
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
        void shouldThrowAccessDenied_whenUserCannotManageProperty() {
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(authenticationService.getCurrentUser()).thenReturn(otherUser);

            assertThatThrownBy(() -> propertyService.uploadPhoto(10L, mockFile))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("permission to manage this property");

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

            assertThatThrownBy(() -> propertyService.changePropertyStatus(10L, PropertyStatus.BLOCKED))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("administrators");
        }

        @Test
        void shouldThrowAccessDenied_whenOtherUserTriesToChangeStatus() {
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(authenticationService.getCurrentUser()).thenReturn(otherUser);

            assertThatThrownBy(() -> propertyService.changePropertyStatus(10L, PropertyStatus.INACTIVE))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("permission");
        }

        @Test
        void shouldActivate_whenPropertyIsDraft() {
            property.setStatus(PropertyStatus.DRAFT);
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(authenticationService.getCurrentUser()).thenReturn(hostUser);
            when(propertyRepository.save(property)).thenReturn(property);
            when(propertyMapper.toDto(property)).thenReturn(responseDto);

            propertyService.changePropertyStatus(10L, PropertyStatus.ACTIVE);

            assertThat(property.getStatus()).isEqualTo(PropertyStatus.ACTIVE);
            verify(propertyRepository).save(property);
        }

        @Test
        void shouldAllowAdminToKeepBlockedPropertyBlocked() {
            property.setStatus(PropertyStatus.BLOCKED);
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(authenticationService.getCurrentUser()).thenReturn(adminUser);
            when(propertyRepository.save(property)).thenReturn(property);
            when(propertyMapper.toDto(property)).thenReturn(responseDto);

            propertyService.changePropertyStatus(10L, PropertyStatus.BLOCKED);

            assertThat(property.getStatus()).isEqualTo(PropertyStatus.BLOCKED);
            verify(propertyRepository).save(property);
        }
    }

    @Nested
    @DisplayName("deleteProperty()")
    class DeletePropertyTests {

        @Test
        void shouldThrowEntityNotFound_whenDeletingNonExistentProperty() {
            when(propertyRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> propertyService.deleteProperty(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Property not found");
        }

        @Test
        void shouldDeleteProperty_whenHostOwnsPropertyAndNoDependencies() {
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(authenticationService.getCurrentUser()).thenReturn(hostUser);
            when(bookingRepository.existsByPropertyId(10L)).thenReturn(false);
            when(reviewRepository.existsByPropertyId(10L)).thenReturn(false);
            when(conversationRepository.existsByPropertyId(10L)).thenReturn(false);

            propertyService.deleteProperty(10L);

            verify(favoriteRepository).deleteByProperty_Id(10L);
            verify(availabilityBlockRepository).deleteAllByPropertyId(10L);
            verify(propertyRepository).delete(property);
        }

        @Test
        void shouldThrowIllegalState_whenPropertyHasBookings() {
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(authenticationService.getCurrentUser()).thenReturn(hostUser);
            when(bookingRepository.existsByPropertyId(10L)).thenReturn(true);

            assertThatThrownBy(() -> propertyService.deleteProperty(10L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("bookings");
        }
    }

    @Nested
    @DisplayName("search()")
    class SearchTests {

        @Test
        void shouldReturnFilteredPageAndApplyTopPrioritySort_whenCriteriaProvided() {
            PropertySearchCriteriaDto criteria = emptyCriteria();
            Page<Property> entityPage = new PageImpl<>(List.of(property), pageable, 1);
            when(propertyRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(entityPage);
            when(propertyMapper.toDto(property)).thenReturn(responseDto);

            Page<PropertyResponseDto> result = propertyService.search(criteria, pageable);
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).id()).isEqualTo(10L);
            verify(propertyValidator).validateSearchCriteria(criteria);
            verify(propertyRepository).findAll(any(Specification.class), pageableCaptor.capture());
            assertTopPrioritySort(pageableCaptor.getValue());
            assertThat(pageableCaptor.getValue().getSort().getOrderFor("id")).isNotNull();
        }
    }

    private void assertTopPrioritySort(Pageable actual) {
        List<Sort.Order> orders = actual.getSort().toList();
        assertThat(orders).hasSizeGreaterThanOrEqualTo(2);
        assertThat(orders.get(0).getProperty()).isEqualTo("isTopPromoted");
        assertThat(orders.get(0).getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(orders.get(1).getProperty()).isEqualTo("topPromotedUntil");
        assertThat(orders.get(1).getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    private PropertyCreateRequestDto requestWithAmenities(List<Long> amenityIds) {
        return new PropertyCreateRequestDto(
                null,
                "Title",
                "Description",
                null,
                "apartment",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                amenityIds,
                null,
                null,
                null
        );
    }

    private PropertySearchCriteriaDto emptyCriteria() {
        return new PropertySearchCriteriaDto(
                null, null, null, null, null,
                null, null, null,
                null, null, null, null, null, null, null,
                null, null,
                null, null, null, null, null, null, null, null,
                null, null,
                null, null, null, null, null, null, null,
                null, null, null, null, null
        );
    }
}
