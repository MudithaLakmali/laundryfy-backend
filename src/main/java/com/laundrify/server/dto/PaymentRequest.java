package com.laundrify.server.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private String orderId;
    private double amount;
    private String method; // e.g., CARD, PAYHERE
}
