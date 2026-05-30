package com.laundrify.server.dto;

import lombok.Data;

@Data
public class PaymentResponse {
    private boolean success;
    private String message;
    private String paymentUrl;
    private String paymentId;
}
