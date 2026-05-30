package com.laundrify.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {
    @Id
    private String id;

    private String userId;
    private String laundryId;
    private String driverId;

    private List<String> items;
    private String serviceType;
    private double price;

    // Status: PENDING, ACCEPTED, REJECTED, DRIVER_ASSIGNED, PICKED_UP, DELIVERED, REVIEWED, CANCELLED
    private String status;
    private String paymentStatus; // PENDING, PARTIAL, PAID, FAILED

    // Pickup location (entered by customer)
    private String pickupAddress;
    private String pickupCity;
    private String pickupDistrict;

    // Denormalized info for display
    private String customerName;
    private String customerPhone;
    private String laundryName;
    private String laundryAddress;
    private String driverName;

    // Review fields (set by laundry after delivery)
    private double weight;          // kg
    private double laundryPrice;    // wash/service cost
    private double deliveryFee;     // delivery charge
    private String reviewNotes;     // optional laundry notes

    // Extended payment & return delivery lifecycle fields
    private String bankReceiptName;
    private String paymentNotes;

    // Customer review & rating fields
    private double laundryRating;
    private String laundryReview;
    private String reviewImagePath;

    private long pickupTimestamp;
    private long deliveryTimestamp;

    private String paymentId;

    private long createdAt;
    private long updatedAt;
}
