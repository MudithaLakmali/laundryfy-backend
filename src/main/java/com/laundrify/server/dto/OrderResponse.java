package com.laundrify.server.dto;

import lombok.Data;

import java.util.List;

@Data
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

    // Pickup location
    private String pickupAddress;
    private String pickupCity;
    private String pickupDistrict;

    // Denormalized info
    private String customerName;
    private String customerPhone;
    private String laundryName;
    private String laundryAddress;
    private String driverName;

    // Review fields
    private double weight;
    private double laundryPrice;
    private double deliveryFee;
    private String reviewNotes;

    // Extended payment & return delivery lifecycle fields
    private String bankReceiptName;
    private String paymentNotes;
    private String receiptImagePath;

    // Customer review & rating fields
    private double laundryRating;
    private String laundryReview;
    private String reviewImagePath;

    // Driver review & rating fields
    private double driverRating;
    private String driverReview;

    private long pickupTimestamp;
    private long deliveryTimestamp;
    private long createdAt;
    private long updatedAt;

    public OrderResponse() {}

    public OrderResponse(String id, String userId, String laundryId, String driverId, List<String> items,
                         String serviceType, double price, String status, String paymentStatus,
                         String pickupAddress, String pickupCity, String pickupDistrict,
                         String customerName, String customerPhone, String laundryName, String laundryAddress, String driverName,
                         double weight, double laundryPrice, double deliveryFee, String reviewNotes,
                         String bankReceiptName, String paymentNotes, String receiptImagePath,
                         double laundryRating, String laundryReview, String reviewImagePath,
                         double driverRating, String driverReview,
                         long pickupTimestamp, long deliveryTimestamp, long createdAt, long updatedAt) {
        this.id = id;
        this.userId = userId;
        this.laundryId = laundryId;
        this.driverId = driverId;
        this.items = items;
        this.serviceType = serviceType;
        this.price = price;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.pickupAddress = pickupAddress;
        this.pickupCity = pickupCity;
        this.pickupDistrict = pickupDistrict;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.laundryName = laundryName;
        this.laundryAddress = laundryAddress;
        this.driverName = driverName;
        this.weight = weight;
        this.laundryPrice = laundryPrice;
        this.deliveryFee = deliveryFee;
        this.reviewNotes = reviewNotes;
        this.bankReceiptName = bankReceiptName;
        this.paymentNotes = paymentNotes;
        this.receiptImagePath = receiptImagePath;
        this.laundryRating = laundryRating;
        this.laundryReview = laundryReview;
        this.reviewImagePath = reviewImagePath;
        this.driverRating = driverRating;
        this.driverReview = driverReview;
        this.pickupTimestamp = pickupTimestamp;
        this.deliveryTimestamp = deliveryTimestamp;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
