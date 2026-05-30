package com.laundrify.server.controller;

import com.laundrify.server.dto.OrderRequest;
import com.laundrify.server.dto.OrderResponse;
import com.laundrify.server.dto.OrderReviewRequest;
import com.laundrify.server.dto.PaymentRequest;
import com.laundrify.server.dto.PaymentResponse;
import com.laundrify.server.model.Order;
import com.laundrify.server.service.OrderService;
import com.laundrify.server.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    // ============== Order CRUD ==============

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest request) {
        Order created = orderService.placeOrder(request);
        OrderResponse resp = toResponse(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable String id) {
        Order o = orderService.getOrderById(id);
        if (o == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        return ResponseEntity.ok(toResponse(o));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUser(@PathVariable String userId) {
        List<Order> list = orderService.getOrdersByUser(userId);
        List<OrderResponse> resp = list.stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/laundry/{laundryId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByLaundry(@PathVariable String laundryId) {
        List<Order> list = orderService.getOrdersByLaundry(laundryId);
        List<OrderResponse> resp = list.stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByDriver(@PathVariable String driverId) {
        List<Order> list = orderService.getOrdersByDriver(driverId);
        List<OrderResponse> resp = list.stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    // ============== Order Lifecycle ==============

    /** Laundry accepts a pending order */
    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptOrder(@PathVariable String id) {
        Order updated = orderService.acceptOrder(id);
        if (updated == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order not found or not in PENDING status");
        return ResponseEntity.ok(toResponse(updated));
    }

    /** Laundry rejects a pending order */
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectOrder(@PathVariable String id) {
        Order updated = orderService.rejectOrder(id);
        if (updated == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order not found or not in PENDING status");
        return ResponseEntity.ok(toResponse(updated));
    }

    /** Get available orders for a driver based on location */
    @GetMapping("/available")
    public ResponseEntity<List<OrderResponse>> getAvailableOrders(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district) {
        List<Order> list = orderService.getAvailableOrdersForDriver(city, district);
        List<OrderResponse> resp = list.stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    /** Driver claims a delivery job */
    @PutMapping("/{id}/assign-driver")
    public ResponseEntity<?> assignDriver(@PathVariable String id, @RequestParam String driverId) {
        Order updated = orderService.assignDriver(id, driverId);
        if (updated == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order not found or not in ACCEPTED status");
        return ResponseEntity.ok(toResponse(updated));
    }

    /** Driver marks order as picked up from customer */
    @PutMapping("/{id}/pickup")
    public ResponseEntity<?> markPickedUp(@PathVariable String id) {
        Order updated = orderService.markPickedUp(id);
        if (updated == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order not found or not in DRIVER_ASSIGNED status");
        return ResponseEntity.ok(toResponse(updated));
    }

    /** Driver marks order as delivered to laundry */
    @PutMapping("/{id}/deliver")
    public ResponseEntity<?> markDelivered(@PathVariable String id) {
        Order updated = orderService.markDelivered(id);
        if (updated == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order not found or not in PICKED_UP status");
        return ResponseEntity.ok(toResponse(updated));
    }

    /** Laundry reviews the order — sets weight, price, delivery fee */
    @PutMapping("/{id}/review")
    public ResponseEntity<?> reviewOrder(@PathVariable String id, @RequestBody OrderReviewRequest request) {
        Order updated = orderService.reviewOrder(id, request);
        if (updated == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order not found or not in DELIVERED status");
        return ResponseEntity.ok(toResponse(updated));
    }

    /** Generic status update (backward compatible) */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestParam String status) {
        Order updated = orderService.updateStatus(id, status);
        if (updated == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        return ResponseEntity.ok(toResponse(updated));
    }

    /** Laundry marks service as completed */
    @PutMapping("/{id}/complete-service")
    public ResponseEntity<?> completeService(@PathVariable String id) {
        Order updated = orderService.completeService(id);
        if (updated == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order not found or not in REVIEWED status");
        return ResponseEntity.ok(toResponse(updated));
    }

    /** Customer uploads mock bank transfer receipt details */
    @PutMapping("/{id}/upload-receipt")
    public ResponseEntity<?> uploadReceipt(
            @PathVariable String id,
            @RequestParam String bankReceiptName,
            @RequestParam(required = false) String paymentNotes) {
        Order updated = orderService.uploadReceipt(id, bankReceiptName, paymentNotes);
        if (updated == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order not found or not in SERVICE_COMPLETED status");
        return ResponseEntity.ok(toResponse(updated));
    }

    /** Laundry confirms receipt payment */
    @PutMapping("/{id}/confirm-payment-receipt")
    public ResponseEntity<?> confirmPaymentReceipt(@PathVariable String id) {
        Order updated = orderService.confirmPaymentReceipt(id);
        if (updated == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order not found or not in RECEIPT_UPLOADED status");
        return ResponseEntity.ok(toResponse(updated));
    }

    /** Driver marks return delivery to customer as completed */
    @PutMapping("/{id}/deliver-to-customer")
    public ResponseEntity<?> deliverToCustomer(@PathVariable String id) {
        Order updated = orderService.markDeliveredToCustomer(id);
        if (updated == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order not found or not in DELIVERY_ASSIGNED status");
        return ResponseEntity.ok(toResponse(updated));
    }

    /** Customer rates and reviews the laundry */
    @PutMapping("/{id}/rate-laundry")
    public ResponseEntity<?> rateLaundry(
            @PathVariable String id,
            @RequestParam double rating,
            @RequestParam(required = false) String reviewText,
            @RequestParam(required = false) MultipartFile reviewImage) {
        Order updated = orderService.rateLaundry(id, rating, reviewText, reviewImage);
        if (updated == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order not found or not in DELIVERED_TO_CUSTOMER status");
        return ResponseEntity.ok(toResponse(updated));
    }

    // ============== Payments ==============

    @PostMapping("/{id}/pay")
    public ResponseEntity<PaymentResponse> initiatePayment(@PathVariable String id, @RequestBody PaymentRequest req) {
        req.setOrderId(id);
        PaymentResponse resp = paymentService.initiatePayment(req);
        if (!resp.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/payments/confirm")
    public ResponseEntity<?> confirmPayment(@RequestParam String paymentId, @RequestParam(required = false) String txId) {
        boolean ok = paymentService.confirmPayment(paymentId, txId);
        if (!ok) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment/order not found");
        return ResponseEntity.ok("Payment confirmed");
    }

    // ============== Mapping ==============

    private OrderResponse toResponse(Order o) {
        return new OrderResponse(
                o.getId(),
                o.getUserId(),
                o.getLaundryId(),
                o.getDriverId(),
                o.getItems(),
                o.getServiceType(),
                o.getPrice(),
                o.getStatus(),
                o.getPaymentStatus(),
                o.getPickupAddress(),
                o.getPickupCity(),
                o.getPickupDistrict(),
                o.getCustomerName(),
                o.getCustomerPhone(),
                o.getLaundryName(),
                o.getLaundryAddress(),
                o.getDriverName(),
                o.getWeight(),
                o.getLaundryPrice(),
                o.getDeliveryFee(),
                o.getReviewNotes(),
                o.getBankReceiptName(),
                o.getPaymentNotes(),
                o.getLaundryRating(),
                o.getLaundryReview(),
                o.getReviewImagePath(),
                o.getPickupTimestamp(),
                o.getDeliveryTimestamp(),
                o.getCreatedAt(),
                o.getUpdatedAt()
        );
    }
}
