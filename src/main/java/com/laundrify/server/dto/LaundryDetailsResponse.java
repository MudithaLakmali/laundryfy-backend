package com.laundrify.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaundryDetailsResponse {
    private String id;
    private String laundryName;
    private String address;
    private String city;
    private String district;
    private String province;
    private String contactNumber;
    private String alternateContactNumber;
    private String website;
    private String description;
    private String servicesOffered;
    private String openingHours;
    private String closingHours;
    private String logoPath;
    private String shopImagePath;
    private java.util.List<String> additionalImagePaths;
    private double averageRating; // Calculated average from all ratings
    private int totalRatings; // Total number of ratings
    private boolean verified;
}
