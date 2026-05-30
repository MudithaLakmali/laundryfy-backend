package com.laundrify.server.dto;

import lombok.Data;

@Data
public class UserResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private boolean enabled;
    
    // Location Details
    private String address;
    private String city;
    private String district;
    private String province;
    private String postalCode;

    public UserResponse() {}

    public UserResponse(String id, String firstName, String lastName, String email, String role, boolean enabled,
                        String address, String city, String district, String province, String postalCode) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.enabled = enabled;
        this.address = address;
        this.city = city;
        this.district = district;
        this.province = province;
        this.postalCode = postalCode;
    }
}
