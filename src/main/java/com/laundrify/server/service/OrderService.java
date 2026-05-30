package com.laundrify.server.service;

import com.laundrify.server.dto.OrderRequest;
import com.laundrify.server.model.Order;
import com.laundrify.server.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public Order placeOrder(OrderRequest request) {
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setLaundryId(request.getLaundryId());
        order.setItems(request.getItems());
        order.setServiceType(request.getServiceType());
        order.setPrice(request.getPrice());
        order.setPickupTimestamp(request.getPickupTimestamp());
        order.setDeliveryTimestamp(request.getDeliveryTimestamp());
        order.setStatus("CREATED");
        order.setPaymentStatus("PENDING");
        long now = System.currentTimeMillis();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        return orderRepository.save(order);
    }

    public Order getOrderById(String id) {
        return orderRepository.findById(id).orElse(null);
    }

    public List<Order> getOrdersByUser(String userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getOrdersByLaundry(String laundryId) {
        return orderRepository.findByLaundryId(laundryId);
    }

    public List<Order> getOrdersByDriver(String driverId) {
        return orderRepository.findByDriverId(driverId);
    }

    public Order updateStatus(String orderId, String status) {
        Optional<Order> maybe = orderRepository.findById(orderId);
        if (maybe.isEmpty()) return null;
        Order order = maybe.get();
        order.setStatus(status);
        order.setUpdatedAt(System.currentTimeMillis());
        return orderRepository.save(order);
    }

    public Order assignDriver(String orderId, String driverId) {
        Optional<Order> maybe = orderRepository.findById(orderId);
        if (maybe.isEmpty()) return null;
        Order order = maybe.get();
        order.setDriverId(driverId);
        order.setUpdatedAt(System.currentTimeMillis());
        return orderRepository.save(order);
    }
}
