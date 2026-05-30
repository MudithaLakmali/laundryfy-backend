package com.laundrify.server.dto;

import lombok.Data;

@Data
public class OrderReviewRequest {
    private double weight;         // kg
    private double laundryPrice;   // wash/service cost in LKR
    private double deliveryFee;    // delivery charge in LKR
    private String reviewNotes;    // optional notes from laundry
}
