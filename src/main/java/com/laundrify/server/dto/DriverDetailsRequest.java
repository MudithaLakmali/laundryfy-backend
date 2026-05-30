package com.laundrify.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverDetailsRequest {
    private String userId;
    private String licenseNumber;
    private String vehicleType;
    private String vehicleModel;
    private String vehicleNumber;
    private String phoneNumber;
    private String address;
}
