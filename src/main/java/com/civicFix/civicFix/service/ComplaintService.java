package com.civicFix.civicFix.service;

import com.civicFix.civicFix.dto.ComplaintRequest;
import com.civicFix.civicFix.dto.ComplaintResponse;
import com.civicFix.civicFix.entity.*;
import com.civicFix.civicFix.exception.DuplicateComplaintException;
import com.civicFix.civicFix.repository.ComplaintRepository;
import com.civicFix.civicFix.repository.MunicipalityRepository;
import com.civicFix.civicFix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final MunicipalityRepository municipalityRepository;

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
                    existing.getLatitude(),
                    existing.getLongitude(),
                    request.getLatitude(),
                    request.getLongitude()
            );

            if (distance <= 100) {
                throw new DuplicateComplaintException(
                        "Duplicate complaint detected within 100 meters in last 30 minutes."
                );
            }
        }

        Municipality municipality =
                municipalityRepository
                        .findByMinLatitudeLessThanEqualAndMaxLatitudeGreaterThanEqualAndMinLongitudeLessThanEqualAndMaxLongitudeGreaterThanEqual(
                                request.getLatitude(),
                                request.getLatitude(),
                                request.getLongitude(),
                                request.getLongitude()
                        )
                        .orElseThrow(() ->
                                new RuntimeException("No municipality found for this location."));

        Complaint complaint = Complaint.builder()
                .imageUrl(request.getImageUrl())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .description(request.getDescription())
                .user(user)
                .municipality(municipality)
                .build();

        Complaint saved = complaintRepository.save(complaint);

        return ComplaintResponse.builder()
                .id(saved.getId())
                .imageUrl(saved.getImageUrl())
                .latitude(saved.getLatitude())
                .longitude(saved.getLongitude())
                .description(saved.getDescription())
                .status(saved.getStatus().name())
                .createdAt(saved.getCreatedAt())
                .resolvedAt(saved.getResolvedAt())
                .build();
    }

    private double calculateDistance(double lat1, double lon1,
                                     double lat2, double lon2) {

        final int R = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c * 1000;
    }

    public ComplaintResponse markAsFixed(Long complaintId, Long adminId) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("User not found"));


        if (admin.getRole() == Role.MUNICIPAL_ADMIN &&
                !admin.getMunicipality().getId()
                        .equals(complaint.getMunicipality().getId())) {

            throw new RuntimeException("Unauthorized access");
        }

        complaint.setStatus(ComplaintStatus.FIXED);
        complaint.setResolvedAt(LocalDateTime.now());

        Complaint saved = complaintRepository.save(complaint);

        sendResolutionSms(saved);

        return mapToResponse(saved);
    }

    private void sendResolutionSms(Complaint complaint) {

        System.out.println(
                "SMS sent to " +
                        complaint.getUser().getPhoneNumber() +
                        " : Your complaint " +
                        complaint.getId() +
                        " has been fixed."
        );
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
                .build();
    }
}