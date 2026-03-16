package com.rentify.core.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    String uploadFile(MultipartFile file);
    void deleteFile(String imageUrl);
}