package com.rentify.core.service;

import com.rentify.core.dto.cloudinary.CloudinaryUploadResult;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    CloudinaryUploadResult uploadFileWithMetadata(MultipartFile file);
    String uploadFile(MultipartFile file);
    void deleteFile(String imageUrl);
    void deleteFileByPublicId(String publicId);
}
