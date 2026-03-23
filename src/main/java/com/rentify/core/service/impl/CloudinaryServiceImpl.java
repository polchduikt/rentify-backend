package com.rentify.core.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.rentify.core.dto.cloudinary.CloudinaryUploadResult;
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

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;
    private static final Pattern VERSION_SEGMENT_PATTERN = Pattern.compile("(?:^|/)v\\d+/(.+)$");
    private static final Set<String> ALLOWED_IMAGE_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    private final Cloudinary cloudinary;

    @Override
    public CloudinaryUploadResult uploadFileWithMetadata(MultipartFile file) {
        validateFile(file);
        try {
            Map<String, Object> options = ObjectUtils.asMap(
                    "resource_type", "auto",
                    "folder", "rentify/properties",
                    "transformation", new Transformation().width(1200).height(800).crop("limit").quality("auto")
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            Object secureUrl = uploadResult.get("secure_url");
            Object publicId = uploadResult.get("public_id");
            if (secureUrl == null || publicId == null) {
                throw new RuntimeException("Cloudinary upload response is missing secure_url or public_id");
            }
            return new CloudinaryUploadResult(secureUrl.toString(), publicId.toString());
        } catch (Exception e) {
            throw new RuntimeException("Image upload failed: " + e.getMessage());
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
        } catch (Exception e) {
            throw new RuntimeException("Image deletion failed: " + e.getMessage());
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
            throw new RuntimeException("Image deletion failed: " + e.getMessage());
        }
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Invalid Cloudinary URL");
        }
        URI uri;
        try {
            uri = URI.create(imageUrl.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid Cloudinary URL");
        }

        if (uri.getHost() == null || !uri.getHost().contains("res.cloudinary.com")) {
            throw new IllegalArgumentException("Invalid Cloudinary URL");
        }

        String path = uri.getPath();
        if (path == null) {
            throw new IllegalArgumentException("Invalid Cloudinary URL format");
        }

        String marker = "/upload/";
        int uploadIndex = path.indexOf(marker);
        if (uploadIndex < 0) {
            throw new IllegalArgumentException("Invalid Cloudinary URL format");
        }

        String afterUpload = path.substring(uploadIndex + marker.length());
        if (afterUpload.isBlank()) {
            throw new IllegalArgumentException("Invalid Cloudinary URL format");
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
            throw new IllegalArgumentException("File is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_MIME_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Unsupported file type. Allowed: JPEG, PNG, WEBP");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File size exceeds 10 MB");
        }
    }
}
