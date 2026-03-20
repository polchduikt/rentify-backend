package com.rentify.core.dto.cloudinary;

public record CloudinaryUploadResult(
        String secureUrl,
        String publicId
) {
}
