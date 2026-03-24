package com.rentify.core.service.impl.property;

import com.rentify.core.dto.cloudinary.CloudinaryUploadResult;
import com.rentify.core.dto.property.PropertyPhotoDto;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.PropertyPhoto;
import com.rentify.core.entity.User;
import com.rentify.core.mapper.PropertyMapper;
import com.rentify.core.repository.PropertyPhotoRepository;
import com.rentify.core.repository.PropertyRepository;
import com.rentify.core.security.UserRoleUtils;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.CloudinaryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PropertyPhotoService {

    private final PropertyRepository propertyRepository;
    private final PropertyPhotoRepository propertyPhotoRepository;
    private final AuthenticationService authenticationService;
    private final CloudinaryService cloudinaryService;
    private final PropertyMapper propertyMapper;

    @Transactional
    public PropertyPhotoDto uploadPhoto(Long propertyId, MultipartFile file) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));
        User currentUser = authenticationService.getCurrentUser();
        assertCanManageProperty(property, currentUser);

        CloudinaryUploadResult uploadResult = cloudinaryService.uploadFileWithMetadata(file);
        PropertyPhoto photo = PropertyPhoto.builder()
                .property(property)
                .url(uploadResult.secureUrl())
                .cloudinaryPublicId(uploadResult.publicId())
                .sortOrder(0)
                .build();
        PropertyPhoto savedPhoto = propertyPhotoRepository.save(photo);
        return propertyMapper.toPhotoDto(savedPhoto);
    }

    @Transactional
    public void deletePhoto(Long propertyId, Long photoId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));
        User currentUser = authenticationService.getCurrentUser();
        assertCanManageProperty(property, currentUser);

        PropertyPhoto photo = propertyPhotoRepository.findByIdAndPropertyId(photoId, propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Photo not found for the property"));
        if (photo.getCloudinaryPublicId() != null && !photo.getCloudinaryPublicId().isBlank()) {
            cloudinaryService.deleteFileByPublicId(photo.getCloudinaryPublicId());
        } else {
            cloudinaryService.deleteFile(photo.getUrl());
        }
        propertyPhotoRepository.delete(photo);
    }

    private void assertCanManageProperty(Property property, User currentUser) {
        boolean isHost = property.getHost().getId().equals(currentUser.getId());
        boolean isAdmin = UserRoleUtils.isAdmin(currentUser);
        if (!isHost && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to manage this property");
        }
    }
}
