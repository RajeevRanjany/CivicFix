package com.civicFix.civicFix.controller;

import com.civicFix.civicFix.dto.ComplaintRequest;
import com.civicFix.civicFix.dto.ComplaintResponse;
import com.civicFix.civicFix.service.ComplaintService;
import com.civicFix.civicFix.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;
    private final ImageUploadService imageUploadService;

    @PostMapping
    public ComplaintResponse createComplaint(
            @RequestBody ComplaintRequest request) {
        return complaintService.createComplaint(request);
    }

    @PutMapping("/{complaintId}/fix/{adminId}")
    public ComplaintResponse fixComplaint(
            @PathVariable Long complaintId,
            @PathVariable Long adminId) {

        return complaintService.markAsFixed(complaintId, adminId);
    }
    @PostMapping("/upload")
    public Map<String,String> uploadImage(
            @RequestParam("file") MultipartFile file) {

        String url = imageUploadService.uploadImage(file);

        return Map.of("imageUrl", url);
    }
}