package com.laundrify.server.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "users")
public class User {
    @Id
    private String id;
    
    private String firstName;
    private String lastName;
    
    @Indexed(unique = true)
    private String email;
    
    private String password;
    
    private String phoneNumber;
    
    // Location Details
    private String address;
    private String city;
    private String district; // Sri Lanka district
    private String postalCode;
    private String province; // Sri Lanka province
    
    private String role; // CUSTOMER, DRIVER, LAUNDRY
    
    private boolean enabled;
    
    private long createdAt;
    private long updatedAt;
}
