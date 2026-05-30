package com.laundrify.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String id;
    private String userId;
    private String laundryId;
    private String driverId;
    private List<String> items;
    private String serviceType;
    private double price;
    private String status;
    private String paymentStatus;
    private long pickupTimestamp;
    private long deliveryTimestamp;
    private long createdAt;
    private long updatedAt;
}
