package com.laundrify.server.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private String userId;
    private String laundryId;
    private List<String> items;
    private String serviceType;
    private double price;
    private long pickupTimestamp;
    private long deliveryTimestamp;
    private String pickupAddress;
    private String pickupCity;
    private String pickupDistrict;
}
