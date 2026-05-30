package com.laundrify.server.repository;

import com.laundrify.server.model.DriverRating;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverRatingRepository extends MongoRepository<DriverRating, String> {
    List<DriverRating> findByDriverId(String driverId);
    List<DriverRating> findByUserId(String userId);
    java.util.Optional<DriverRating> findByDriverIdAndUserId(String driverId, String userId);
}
