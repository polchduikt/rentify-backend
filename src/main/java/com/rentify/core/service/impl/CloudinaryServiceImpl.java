package com.rentify.core.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.rentify.core.config.CloudinaryProperties;
import com.rentify.core.config.MediaUploadProperties;
import com.rentify.core.dto.cloudinary.CloudinaryUploadResult;
import com.rentify.core.exception.DomainException;
import com.rentify.core.exception.FileUploadException;
import com.rentify.core.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {
    private static final Pattern VERSION_SEGMENT_PATTERN = Pattern.compile("(?:^|/)v\\d+/(.+)$");

    private final Cloudinary cloudinary;
    private final MediaUploadProperties mediaUploadProperties;
    private final CloudinaryProperties cloudinaryProperties;

    @Override
    public CloudinaryUploadResult uploadFileWithMetadata(MultipartFile file) {
        validateFile(file);
        try {
            Map<String, Object> options = ObjectUtils.asMap(
                    "resource_type", "auto",
                    "folder", cloudinaryProperties.getFolder(),
                    "transformation", new Transformation()
                            .width(cloudinaryProperties.getMaxWidth())
                            .height(cloudinaryProperties.getMaxHeight())
                            .crop("limit")
                            .quality("auto")
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            Object secureUrl = uploadResult.get("secure_url");
            Object publicId = uploadResult.get("public_id");
            if (secureUrl == null || publicId == null) {
                throw new FileUploadException("Cloudinary upload response is missing secure_url or public_id");
            }
            return new CloudinaryUploadResult(secureUrl.toString(), publicId.toString());
        } catch (FileUploadException ex) {
            throw ex;
        } catch (Exception e) {
            throw new FileUploadException("Image upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file) {
        return uploadFileWithMetadata(file).secureUrl();
    }

    @Override
    public void deleteFile(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        try {
            String publicId = extractPublicIdFromUrl(imageUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (DomainException ex) {
            throw ex;
        } catch (Exception e) {
            throw new FileUploadException("Image deletion failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFileByPublicId(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (Exception e) {
            throw new FileUploadException("Image deletion failed: " + e.getMessage(), e);
        }
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw DomainException.badRequest("CLOUDINARY_URL_INVALID", "Invalid Cloudinary URL");
        }
        URI uri;
        try {
            uri = URI.create(imageUrl.trim());
        } catch (IllegalArgumentException ex) {
            throw DomainException.badRequest("CLOUDINARY_URL_INVALID", "Invalid Cloudinary URL");
        }

        if (uri.getHost() == null || !uri.getHost().contains("res.cloudinary.com")) {
            throw DomainException.badRequest("CLOUDINARY_URL_INVALID", "Invalid Cloudinary URL");
        }

        String path = uri.getPath();
        if (path == null) {
            throw DomainException.badRequest("CLOUDINARY_URL_INVALID", "Invalid Cloudinary URL format");
        }

        String marker = "/upload/";
        int uploadIndex = path.indexOf(marker);
        if (uploadIndex < 0) {
            throw DomainException.badRequest("CLOUDINARY_URL_INVALID", "Invalid Cloudinary URL format");
        }

        String afterUpload = path.substring(uploadIndex + marker.length());
        if (afterUpload.isBlank()) {
            throw DomainException.badRequest("CLOUDINARY_URL_INVALID", "Invalid Cloudinary URL format");
        }

        String candidate = afterUpload;
        Matcher matcher = VERSION_SEGMENT_PATTERN.matcher(afterUpload);
        if (matcher.find()) {
            candidate = matcher.group(1);
        }

        int dotIndex = candidate.lastIndexOf('.');
        if (dotIndex <= 0) {
            return candidate;
        }
        return candidate.substring(0, dotIndex);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw DomainException.badRequest("FILE_REQUIRED", "File is required");
        }

        String contentType = file.getContentType();
        Set<String> allowed = mediaUploadProperties.getAllowedMimeTypes();
        if (contentType == null || allowed == null || !allowed.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw DomainException.badRequest("FILE_TYPE_NOT_ALLOWED", "Unsupported file type. Allowed: JPEG, PNG, WEBP");
        }

        if (file.getSize() > mediaUploadProperties.getMaxFileSizeBytes()) {
            throw DomainException.badRequest("FILE_TOO_LARGE", "File size exceeds 10 MB");
        }
    }
}
