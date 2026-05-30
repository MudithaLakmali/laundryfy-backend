package com.laundrify.server.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class LaundrySignupRequest {
    // Business Credentials
    private String email;
    private String password;
    private String confirmPassword;
    
    // Business Information
    private String laundryName;
    private String businessRegistrationNumber;
    private String taxId;
    private String description;
    private String servicesOffered;
    private String openingHours;
    private String closingHours;
    
    // Ownership Details
    private String ownerFirstName;
    private String ownerLastName;
    private String ownerContactNumber;
    private String ownerEmail;
    
    // Location Details
    private String address;
    private String city;
    private String district; // Sri Lanka district
    private String postalCode;
    private String province; // Sri Lanka province/district
    private String contactNumber;
    private String alternateContactNumber;
    private String website;

    // Business Media
    private MultipartFile logo;
    private MultipartFile shopImage;
    private MultipartFile additionalImage1;
    private MultipartFile additionalImage2;

}
