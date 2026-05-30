package com.laundrify.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverRatingRequest {
    private String driverId;
    private double rating; // 1-5 stars
    private String review; // Optional
    private String[] images; // Optional image paths
}
