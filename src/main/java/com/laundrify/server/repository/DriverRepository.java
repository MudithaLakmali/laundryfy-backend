package com.laundrify.server.repository;

import com.laundrify.server.model.Driver;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends MongoRepository<Driver, String> {
    Optional<Driver> findByUserId(String userId);
    boolean existsByUserId(String userId);

    // Location-based lookup for driver matching
    List<Driver> findByCityAndDistrict(String city, String district);
    List<Driver> findByCity(String city);
}
