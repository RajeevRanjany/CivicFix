package com.civicFix.civicFix.dto;

import lombok.Data;

@Data
public class ComplaintRequest {

    private String imageUrl;
    private double latitude;
    private double longitude;
    private String description;
}