package com.civicFix.civicFix.controller;

import com.civicFix.civicFix.dto.ComplaintRequest;
import com.civicFix.civicFix.dto.ComplaintResponse;
import com.civicFix.civicFix.service.ComplaintService;
import com.civicFix.civicFix.service.ImageUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;
    private final ImageUploadService imageUploadService;

    @PostMapping("/api/complaints")
    public ComplaintResponse createComplaint(@Valid @RequestBody ComplaintRequest request) {
        return complaintService.createComplaint(request);
    }

    @GetMapping("/api/complaints/my")
    public List<ComplaintResponse> getMyComplaints() {
        return complaintService.getMyComplaints();
    }

    @GetMapping("/api/complaints")
    public List<ComplaintResponse> getAllComplaints(
            @RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return complaintService.getComplaintsByStatus(status);
        }
        return complaintService.getAllComplaints();
    }

    @PutMapping("/api/complaints/{complaintId}/fix/{adminId}")
    public ComplaintResponse fixComplaint(
            @PathVariable Long complaintId,
            @PathVariable Long adminId) {
        return complaintService.markAsFixed(complaintId, adminId);
    }

    @PostMapping("/api/upload")
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = imageUploadService.uploadImage(file);
        return Map.of("imageUrl", url);
    }
}
