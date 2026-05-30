package com.laundrify.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaundryRegistrationRequest {
    private String userId;
    private String laundryName;
    private String location;
    private String contactNumber;
    private String alternateContactNumber;
    private String contactEmail;
    private String website;
    private String description;
    private String openingHours;
    private String closingHours;
    private String servicesOffered;
}
