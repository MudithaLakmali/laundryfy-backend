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

    private String status; // e.g., CREATED, PICKED_UP, IN_PROCESS, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
    private String paymentStatus; // PENDING, PARTIAL, PAID, FAILED

    private long pickupTimestamp;
    private long deliveryTimestamp;

    private String paymentId;

    private long createdAt;
    private long updatedAt;
}
