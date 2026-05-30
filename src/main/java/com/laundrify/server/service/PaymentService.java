package com.laundrify.server.service;

import com.laundrify.server.dto.PaymentRequest;
import com.laundrify.server.dto.PaymentResponse;
import com.laundrify.server.model.Order;
import com.laundrify.server.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;

    // Create a simple stub for initiating payments (e.g., PayHere integration point)
    public PaymentResponse initiatePayment(PaymentRequest request) {
        PaymentResponse resp = new PaymentResponse();

        // generate a mock payment id and a fake redirect URL (in real app call PayHere)
        String paymentId = "pay_" + UUID.randomUUID();
        String paymentUrl = "https://payhere.mock/checkout/" + paymentId + "?amount=" + request.getAmount();

        // update order with payment id and mark paymentStatus as PENDING
        Order order = orderRepository.findById(request.getOrderId()).orElse(null);
        if (order == null) {
            resp.setSuccess(false);
            resp.setMessage("Order not found");
            return resp;
        }

        order.setPaymentId(paymentId);
        order.setPaymentStatus("PENDING");
        orderRepository.save(order);

        resp.setSuccess(true);
        resp.setMessage("Payment initiated");
        resp.setPaymentId(paymentId);
        resp.setPaymentUrl(paymentUrl);

        return resp;
    }

    // Confirm payment (webhook or callback) - simple implementation
    public boolean confirmPayment(String paymentId, String externalTxId) {
        var maybe = orderRepository.findByPaymentId(paymentId);
        if (maybe.isEmpty()) return false;

        Order order = maybe.get();
        order.setPaymentStatus("PAID");
        orderRepository.save(order);
        return true;
    }
}
