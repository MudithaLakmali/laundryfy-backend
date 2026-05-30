package com.laundrify.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "laundry_ratings")
public class LaundryRating {
    @Id
    private String id;
    
    private String laundryId;
    private String userId; // Customer who gave the rating
    private String customerName;
    
    private double rating; // 1-5 stars
    private String review; // Optional review message
    private String[] images; // Optional images from customer
    
    private long createdAt;
    private long updatedAt;
}
