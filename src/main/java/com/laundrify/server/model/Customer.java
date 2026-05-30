package com.laundrify.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "customers")
public class Customer {
    @Id
    private String id;
    
    private String userId;
    
    private String firstName;
    private String lastName;
    
    private String email;
    private String phoneNumber;
    
    // Location Details
    private String address;
    private String city;
    private String district; // Sri Lanka district
    private String postalCode;
    private String province; // Sri Lanka province
    
    private boolean verified;
    
    private long createdAt;
    private long updatedAt;
}
