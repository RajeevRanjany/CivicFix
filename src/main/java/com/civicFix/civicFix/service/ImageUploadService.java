package com.civicFix.civicFix.service;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) {

        try {
            Map uploadResult = cloudinary.uploader()
                    .upload(file.getBytes(), Map.of());

            return uploadResult.get("url").toString();

        } catch (Exception e) {
            throw new RuntimeException("Image upload failed");
        }
    }
}
