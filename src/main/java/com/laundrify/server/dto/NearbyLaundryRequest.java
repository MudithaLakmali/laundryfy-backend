package com.laundrify.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearbyLaundryRequest {
    private String city;
    private String district;
    private String province;
    private int radiusKm; // Optional: search radius in kilometers
}
