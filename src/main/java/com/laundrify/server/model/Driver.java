package com.laundrify.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "drivers")
public class Driver {
    @Id
    private String id;
    
    private String userId;
    
    // Basic Information
    private String firstName;
    private String lastName;
    private String email;
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
    private String province; // Sri Lanka province
    private String zipCode;
    
    // Document paths
    private String licenseImagePath;
    private String vehicleImagePath;
    private String profileImagePath;
    
    private boolean verified;
    
    private long createdAt;
    private long updatedAt;
}

