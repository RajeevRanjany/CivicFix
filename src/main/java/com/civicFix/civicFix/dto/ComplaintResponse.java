package com.civicFix.civicFix.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ComplaintResponse {

    private Long id;
    private String imageUrl;
    private double latitude;
    private double longitude;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private String municipalityName;
    private String userName;
    private String userPhone;
}