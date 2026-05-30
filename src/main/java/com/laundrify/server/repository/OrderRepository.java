package com.laundrify.server.repository;

import com.laundrify.server.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserId(String userId);
    List<Order> findByLaundryId(String laundryId);
    List<Order> findByDriverId(String driverId);
    Optional<Order> findByPaymentId(String paymentId);

    // For driver: find accepted orders by pickup location
    List<Order> findByStatusAndPickupCityAndPickupDistrict(String status, String city, String district);
    List<Order> findByStatusAndPickupCity(String status, String city);
    List<Order> findByStatus(String status);
}
