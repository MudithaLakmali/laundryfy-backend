package com.laundrify.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "laundries")
public class Laundry {
    @Id
    private String id;

    private String userId;
    
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
    private String ownerEmail;
    
    // Location Details
    private String address;
    private String city;
    private String district; // Sri Lanka district
    private String postalCode;
    private String province; // Sri Lanka province
    private String location;
    private String contactNumber;
    private String alternateContactNumber;
    private String website;
    private String contactEmail;

    // Media
    private String logoPath;
    private String shopImagePath;
    private List<String> additionalImagePaths;

    private boolean verified;
    
    private long createdAt;
    private long updatedAt;
}

