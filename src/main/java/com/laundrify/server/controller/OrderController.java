package com.laundrify.server.controller;

import com.laundrify.server.dto.OrderRequest;
import com.laundrify.server.dto.OrderResponse;
import com.laundrify.server.dto.PaymentRequest;
import com.laundrify.server.dto.PaymentResponse;
import com.laundrify.server.model.Order;
import com.laundrify.server.service.OrderService;
import com.laundrify.server.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

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

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestParam String status) {
        Order updated = orderService.updateStatus(id, status);
        if (updated == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        return ResponseEntity.ok(toResponse(updated));
    }

    @PutMapping("/{id}/assign-driver")
    public ResponseEntity<?> assignDriver(@PathVariable String id, @RequestParam String driverId) {
        Order updated = orderService.assignDriver(id, driverId);
        if (updated == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        return ResponseEntity.ok(toResponse(updated));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<PaymentResponse> initiatePayment(@PathVariable String id, @RequestBody PaymentRequest req) {
        // enforce orderId
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
                o.getPickupTimestamp(),
                o.getDeliveryTimestamp(),
                o.getCreatedAt(),
                o.getUpdatedAt()
        );
    }
}
