package com.laundrify.server.service;

import com.laundrify.server.dto.OrderRequest;
import com.laundrify.server.dto.OrderReviewRequest;
import com.laundrify.server.model.Customer;
import com.laundrify.server.model.Driver;
import com.laundrify.server.model.Laundry;
import com.laundrify.server.model.Order;
import com.laundrify.server.repository.CustomerRepository;
import com.laundrify.server.repository.DriverRepository;
import com.laundrify.server.repository.LaundryRepository;
import com.laundrify.server.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final LaundryRepository laundryRepository;
    private final DriverRepository driverRepository;
    private final RatingService ratingService;

    @Value("${file.upload.review.dir:uploads/reviews}")
    private String uploadDir;

    @Value("${file.upload.receipt.dir:uploads/receipts}")
    private String receiptUploadDir;

    /**
     * Customer places a new order — status = PENDING
     */
    public Order placeOrder(OrderRequest request) {
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setLaundryId(request.getLaundryId());
        order.setItems(request.getItems());
        order.setServiceType(request.getServiceType());
        order.setPrice(request.getPrice());
        order.setPickupTimestamp(request.getPickupTimestamp());
        order.setDeliveryTimestamp(request.getDeliveryTimestamp());

        // Pickup location
        order.setPickupAddress(request.getPickupAddress());
        order.setPickupCity(request.getPickupCity());
        order.setPickupDistrict(request.getPickupDistrict());

        // Denormalize customer info
        Customer customer = customerRepository.findByUserId(request.getUserId()).orElse(null);
        if (customer != null) {
            order.setCustomerName(customer.getFirstName() + " " + customer.getLastName());
            order.setCustomerPhone(customer.getPhoneNumber());
            // If pickup address not provided, use customer address
            if (order.getPickupAddress() == null || order.getPickupAddress().isEmpty()) {
                order.setPickupAddress(customer.getAddress());
            }
            if (order.getPickupCity() == null || order.getPickupCity().isEmpty()) {
                order.setPickupCity(customer.getCity());
            }
            if (order.getPickupDistrict() == null || order.getPickupDistrict().isEmpty()) {
                order.setPickupDistrict(customer.getDistrict());
            }
        }

        // Denormalize laundry info
        Laundry laundry = laundryRepository.findById(request.getLaundryId()).orElse(null);
        if (laundry != null) {
            order.setLaundryName(laundry.getLaundryName());
            order.setLaundryAddress(laundry.getAddress());
        }

        order.setStatus("PENDING");
        order.setPaymentStatus("PENDING");
        long now = System.currentTimeMillis();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        return orderRepository.save(order);
    }

    /**
     * Get order by ID
     */
    public Order getOrderById(String id) {
        return orderRepository.findById(id).orElse(null);
    }

    /**
     * Get orders by customer userId
     */
    public List<Order> getOrdersByUser(String userId) {
        return orderRepository.findByUserId(userId);
    }

    /**
     * Get orders by laundry ID
     */
    public List<Order> getOrdersByLaundry(String laundryId) {
        return orderRepository.findByLaundryId(laundryId);
    }

    /**
     * Get orders by driver ID
     */
    public List<Order> getOrdersByDriver(String driverId) {
        return orderRepository.findByDriverId(driverId);
    }

    /**
     * Laundry accepts the order — PENDING → ACCEPTED
     */
    public Order acceptOrder(String orderId) {
        Optional<Order> maybe = orderRepository.findById(orderId);
        if (maybe.isEmpty()) return null;

        Order order = maybe.get();
        if (!"PENDING".equals(order.getStatus())) return null;

        order.setStatus("ACCEPTED");
        order.setUpdatedAt(System.currentTimeMillis());
        return orderRepository.save(order);
    }

    /**
     * Laundry rejects the order — PENDING → REJECTED
     */
    public Order rejectOrder(String orderId) {
        Optional<Order> maybe = orderRepository.findById(orderId);
        if (maybe.isEmpty()) return null;

        Order order = maybe.get();
        if (!"PENDING".equals(order.getStatus())) return null;

        order.setStatus("REJECTED");
        order.setUpdatedAt(System.currentTimeMillis());
        return orderRepository.save(order);
    }

    /**
     * Get available orders for a driver based on their registered location.
     * Returns ACCEPTED orders matching the driver's city/district.
     */
    public List<Order> getAvailableOrdersForDriver(String city, String district) {
        List<Order> accepted = new ArrayList<>();
        List<Order> readyForDelivery = new ArrayList<>();

        // 1. Fetch available pickups (ACCEPTED)
        if (city != null && district != null) {
            accepted = orderRepository.findByStatusAndPickupCityAndPickupDistrict("ACCEPTED", city, district);
        }
        if (accepted.isEmpty() && city != null) {
            accepted = orderRepository.findByStatusAndPickupCity("ACCEPTED", city);
        }
        if (accepted.isEmpty()) {
            accepted = orderRepository.findByStatus("ACCEPTED");
        }

        // 2. Fetch available return deliveries (READY_FOR_DELIVERY)
        if (city != null && district != null) {
            readyForDelivery = orderRepository.findByStatusAndPickupCityAndPickupDistrict("READY_FOR_DELIVERY", city, district);
        }
        if (readyForDelivery.isEmpty() && city != null) {
            readyForDelivery = orderRepository.findByStatusAndPickupCity("READY_FOR_DELIVERY", city);
        }
        if (readyForDelivery.isEmpty()) {
            readyForDelivery = orderRepository.findByStatus("READY_FOR_DELIVERY");
        }

        // 3. Combine both lists
        List<Order> combined = new ArrayList<>();
        combined.addAll(accepted);
        combined.addAll(readyForDelivery);
        return combined;
    }

    /**
     * Driver claims a delivery job — ACCEPTED → DRIVER_ASSIGNED
     */
    public Order assignDriver(String orderId, String driverId) {
        Optional<Order> maybe = orderRepository.findById(orderId);
        if (maybe.isEmpty()) return null;

        Order order = maybe.get();
        String currentStatus = order.getStatus();

        if ("ACCEPTED".equals(currentStatus)) {
            order.setDriverId(driverId);
            order.setStatus("DRIVER_ASSIGNED");
        } else if ("READY_FOR_DELIVERY".equals(currentStatus)) {
            order.setDriverId(driverId);
            order.setStatus("DELIVERY_ASSIGNED");
        } else {
            return null;
        }

        // Denormalize driver info
        Driver driver = driverRepository.findById(driverId).orElse(null);
        if (driver == null) {
            // Try by userId
            driver = driverRepository.findByUserId(driverId).orElse(null);
        }
        if (driver != null) {
            order.setDriverName(driver.getFirstName() + " " + driver.getLastName());
        }

        order.setUpdatedAt(System.currentTimeMillis());
        return orderRepository.save(order);
    }

    /**
     * Driver picked up clothes from customer — DRIVER_ASSIGNED → PICKED_UP
     */
    public Order markPickedUp(String orderId) {
        Optional<Order> maybe = orderRepository.findById(orderId);
        if (maybe.isEmpty()) return null;

        Order order = maybe.get();
        if (!"DRIVER_ASSIGNED".equals(order.getStatus())) return null;

        order.setStatus("PICKED_UP");
        order.setPickupTimestamp(System.currentTimeMillis());
        order.setUpdatedAt(System.currentTimeMillis());
        return orderRepository.save(order);
    }

    /**
     * Driver delivered clothes to laundry — PICKED_UP → DELIVERED
     */
    public Order markDelivered(String orderId) {
        Optional<Order> maybe = orderRepository.findById(orderId);
        if (maybe.isEmpty()) return null;

        Order order = maybe.get();
        if (!"PICKED_UP".equals(order.getStatus())) return null;

        order.setStatus("DELIVERED");
        order.setDeliveryTimestamp(System.currentTimeMillis());
        order.setUpdatedAt(System.currentTimeMillis());
        return orderRepository.save(order);
    }

    /**
     * Laundry reviews the order: sets weight, price, delivery fee — DELIVERED → REVIEWED
     */
    public Order reviewOrder(String orderId, OrderReviewRequest reviewRequest) {
        Optional<Order> maybe = orderRepository.findById(orderId);
        if (maybe.isEmpty()) return null;

        Order order = maybe.get();
        if (!"DELIVERED".equals(order.getStatus())) return null;

        order.setWeight(reviewRequest.getWeight());
        order.setLaundryPrice(reviewRequest.getLaundryPrice());
        order.setDeliveryFee(reviewRequest.getDeliveryFee());
        order.setReviewNotes(reviewRequest.getReviewNotes());

        // Total price = laundry price + delivery fee
        order.setPrice(reviewRequest.getLaundryPrice() + reviewRequest.getDeliveryFee());

        order.setStatus("REVIEWED");
        order.setUpdatedAt(System.currentTimeMillis());
        return orderRepository.save(order);
    }

    /**
     * Generic status update (kept for backward compatibility)
     */
    public Order updateStatus(String orderId, String status) {
        Optional<Order> maybe = orderRepository.findById(orderId);
        if (maybe.isEmpty()) return null;
        Order order = maybe.get();
        order.setStatus(status);
        order.setUpdatedAt(System.currentTimeMillis());
        return orderRepository.save(order);
    }

    /**
     * Laundry marks the service as completed — REVIEWED → SERVICE_COMPLETED
     */
    public Order completeService(String orderId) {
        Optional<Order> maybe = orderRepository.findById(orderId);
        if (maybe.isEmpty()) return null;

        Order order = maybe.get();
        if (!"REVIEWED".equals(order.getStatus())) return null;

        order.setStatus("SERVICE_COMPLETED");
        order.setUpdatedAt(System.currentTimeMillis());
        return orderRepository.save(order);
    }

    /**
     * Customer uploads receipt notes + optional image — SERVICE_COMPLETED → RECEIPT_UPLOADED
     */
    public Order uploadReceipt(String orderId, String bankReceiptName, String paymentNotes, MultipartFile receiptImage) {
        Optional<Order> maybe = orderRepository.findById(orderId);
        if (maybe.isEmpty()) return null;

        Order order = maybe.get();
        if (!"SERVICE_COMPLETED".equals(order.getStatus())) return null;

        // Save receipt image if provided
        if (receiptImage != null && !receiptImage.isEmpty()) {
            try {
                String fileName = "receipt_" + UUID.randomUUID() + "_" + receiptImage.getOriginalFilename();
                Path filePath = Paths.get(receiptUploadDir, fileName);
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, receiptImage.getBytes());
                order.setReceiptImagePath(filePath.toString());
            } catch (IOException e) {
                log.error("Failed to save receipt image", e);
            }
        }

        order.setBankReceiptName(bankReceiptName);
        order.setPaymentNotes(paymentNotes);
        order.setStatus("RECEIPT_UPLOADED");
        order.setPaymentStatus("RECEIPT_UPLOADED");
        order.setUpdatedAt(System.currentTimeMillis());
        return orderRepository.save(order);
    }

    /**
     * Laundry confirms the uploaded payment receipt — RECEIPT_UPLOADED → READY_FOR_DELIVERY
     */
    public Order confirmPaymentReceipt(String orderId) {
        Optional<Order> maybe = orderRepository.findById(orderId);
        if (maybe.isEmpty()) return null;

        Order order = maybe.get();
        if (!"RECEIPT_UPLOADED".equals(order.getStatus())) return null;

        order.setStatus("READY_FOR_DELIVERY");
        order.setPaymentStatus("PAID");

        // Clear driver fields so any nearby driver can claim the return delivery job!
        order.setDriverId(null);
        order.setDriverName(null);

        order.setUpdatedAt(System.currentTimeMillis());
        return orderRepository.save(order);
    }

    /**
     * Driver marks return delivery as completed — DELIVERY_ASSIGNED → DELIVERED_TO_CUSTOMER
     */
    public Order markDeliveredToCustomer(String orderId) {
        Optional<Order> maybe = orderRepository.findById(orderId);
        if (maybe.isEmpty()) return null;

        Order order = maybe.get();
        if (!"DELIVERY_ASSIGNED".equals(order.getStatus())) return null;

        order.setStatus("DELIVERED_TO_CUSTOMER");
        order.setDeliveryTimestamp(System.currentTimeMillis());
        order.setUpdatedAt(System.currentTimeMillis());
        return orderRepository.save(order);
    }

    /**
     * Customer rates laundry and writes review — DELIVERED_TO_CUSTOMER → COMPLETED
     */
    public Order rateLaundry(String orderId, double rating, String reviewText, MultipartFile reviewImage) {
        Optional<Order> maybe = orderRepository.findById(orderId);
        if (maybe.isEmpty()) return null;

        Order order = maybe.get();
        if (!"DELIVERED_TO_CUSTOMER".equals(order.getStatus())) return null;

        String reviewImagePath = null;
        if (reviewImage != null && !reviewImage.isEmpty()) {
            try {
                reviewImagePath = saveFile(reviewImage, "review");
            } catch (IOException e) {
                log.error("Failed to save review image", e);
            }
        }

        order.setLaundryRating(rating);
        order.setLaundryReview(reviewText);
        order.setReviewImagePath(reviewImagePath);
        order.setStatus("COMPLETED");
        order.setUpdatedAt(System.currentTimeMillis());

        // Invoke RatingService to add the rating to the laundry's overall score
        try {
            com.laundrify.server.dto.LaundryRatingRequest ratingRequest = new com.laundrify.server.dto.LaundryRatingRequest();
            ratingRequest.setLaundryId(order.getLaundryId());
            ratingRequest.setRating(rating);
            ratingRequest.setReview(reviewText);
            ratingService.addLaundryRating(order.getUserId(), ratingRequest);
        } catch (Exception e) {
            // Log warning but don't break order status flow
            log.warn("Warning: failed to add rating to RatingService: {}", e.getMessage());
        }

        return orderRepository.save(order);
    }

    /**
     * Customer rates the driver — does NOT change order status
     */
    public Order rateDriver(String orderId, double driverRating, String driverReviewText) {
        Optional<Order> maybe = orderRepository.findById(orderId);
        if (maybe.isEmpty()) return null;

        Order order = maybe.get();
        // Allow driver rating once order is DELIVERED_TO_CUSTOMER or COMPLETED
        if (!"DELIVERED_TO_CUSTOMER".equals(order.getStatus()) && !"COMPLETED".equals(order.getStatus())) return null;
        if (order.getDriverId() == null) return null;

        order.setDriverRating(driverRating);
        order.setDriverReview(driverReviewText);
        order.setUpdatedAt(System.currentTimeMillis());

        // Persist to DriverRating collection
        try {
            com.laundrify.server.dto.DriverRatingRequest ratingRequest = new com.laundrify.server.dto.DriverRatingRequest();
            ratingRequest.setDriverId(order.getDriverId());
            ratingRequest.setRating(driverRating);
            ratingRequest.setReview(driverReviewText);
            ratingService.addDriverRating(order.getUserId(), ratingRequest);
        } catch (Exception e) {
            log.warn("Warning: failed to add driver rating to RatingService: {}", e.getMessage());
        }

        return orderRepository.save(order);
    }

    private String saveFile(MultipartFile file, String type) throws IOException {
        String fileName = type + "_" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, file.getBytes());
        return filePath.toString();
    }
}
