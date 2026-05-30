package com.laundrify.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverSignupRequest {
    // Basic Information
    private String email;
    private String password;
    private String confirmPassword;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    
    // Driver Information
    private String licenseNumber;
    private String licenseExpiryDate;
    private String vehicleType;
    private String vehicleModel;
    private String vehicleNumber;
    private String vehicleRegistrationNumber;
    private String vehicleInsuranceNumber;
    
    // Location Details
    private String address;
    private String city;
    private String district; // Sri Lanka district
    private String postalCode;
    private String province; // Sri Lanka province/district
    private String zipCode;

    // Required Images
    private MultipartFile selfieImage;
    private MultipartFile vehicleNumberPlateImage;
}
