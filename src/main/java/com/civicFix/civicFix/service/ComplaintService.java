package com.civicFix.civicFix.service;

import com.civicFix.civicFix.dto.ComplaintRequest;
import com.civicFix.civicFix.dto.ComplaintResponse;
import com.civicFix.civicFix.entity.*;
import com.civicFix.civicFix.exception.DuplicateComplaintException;
import com.civicFix.civicFix.repository.ComplaintRepository;
import com.civicFix.civicFix.repository.MunicipalityRepository;
import com.civicFix.civicFix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final MunicipalityRepository municipalityRepository;

    @Transactional
    public ComplaintResponse createComplaint(ComplaintRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        List<Complaint> recentComplaints =
                complaintRepository.findByUserIdAndCreatedAtAfter(userId, thirtyMinutesAgo);

        for (Complaint existing : recentComplaints) {
            double distance = calculateDistance(
                    existing.getLatitude(), existing.getLongitude(),
                    request.getLatitude(), request.getLongitude()
            );
            if (distance <= 100) {
                throw new DuplicateComplaintException(
                        "Duplicate complaint detected within 100 meters in last 30 minutes.");
            }
        }

        Municipality municipality =
                municipalityRepository
                        .findByMinLatitudeLessThanEqualAndMaxLatitudeGreaterThanEqualAndMinLongitudeLessThanEqualAndMaxLongitudeGreaterThanEqual(
                                request.getLatitude(), request.getLatitude(),
                                request.getLongitude(), request.getLongitude()
                        )
                        .orElse(null);

        Complaint complaint = Complaint.builder()
                .imageUrl(request.getImageUrl())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .description(request.getDescription())
                .user(user)
                .municipality(municipality)
                .build();

        Complaint saved = complaintRepository.save(complaint);

        // If no municipality found, override the default CREATED status
        if (municipality == null) {
            saved.setStatus(ComplaintStatus.NO_MUNICIPALITY_FOUND);
            saved = complaintRepository.save(saved);
            log.warn("Complaint {} saved with no municipality for location ({}, {})",
                    saved.getId(), request.getLatitude(), request.getLongitude());
        } else {
            log.info("Complaint {} created by user {}", saved.getId(), userId);
        }
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> getMyComplaints() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        return complaintRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> getComplaintsByStatus(String status) {
        ComplaintStatus cs = ComplaintStatus.valueOf(status.toUpperCase());
        return complaintRepository.findByStatus(cs)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public ComplaintResponse markAsFixed(Long complaintId, Long adminId) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (admin.getRole() == Role.MUNICIPAL_ADMIN &&
                !admin.getMunicipality().getId().equals(complaint.getMunicipality().getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        complaint.setStatus(ComplaintStatus.FIXED);
        complaint.setResolvedAt(LocalDateTime.now());

        Complaint saved = complaintRepository.save(complaint);
        log.info("Complaint {} marked as fixed by admin {}", complaintId, adminId);
        sendResolutionSms(saved);
        return mapToResponse(saved);
    }

    private void sendResolutionSms(Complaint complaint) {
        log.info("SMS sent to {} : Your complaint {} has been fixed.",
                complaint.getUser().getPhoneNumber(), complaint.getId());
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000;
    }

    private ComplaintResponse mapToResponse(Complaint complaint) {
        return ComplaintResponse.builder()
                .id(complaint.getId())
                .imageUrl(complaint.getImageUrl())
                .latitude(complaint.getLatitude())
                .longitude(complaint.getLongitude())
                .description(complaint.getDescription())
                .status(complaint.getStatus().name())
                .createdAt(complaint.getCreatedAt())
                .resolvedAt(complaint.getResolvedAt())
                .municipalityName(complaint.getMunicipality() != null
                        ? complaint.getMunicipality().getName() : null)
                .userName(complaint.getUser() != null ? complaint.getUser().getName() : null)
                .userPhone(complaint.getUser() != null ? complaint.getUser().getPhoneNumber() : null)
                .build();
    }
}
