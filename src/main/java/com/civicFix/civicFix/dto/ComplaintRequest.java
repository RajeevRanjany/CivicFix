package com.civicFix.civicFix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ComplaintRequest {

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    @NotNull(message = "Latitude is required")
    private double latitude;

    @NotNull(message = "Longitude is required")
    private double longitude;

    @NotBlank(message = "Description is required")
    private String description;
}