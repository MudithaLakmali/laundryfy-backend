package com.laundrify.server.repository;

import com.laundrify.server.model.LaundryRating;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaundryRatingRepository extends MongoRepository<LaundryRating, String> {
    List<LaundryRating> findByLaundryId(String laundryId);
    List<LaundryRating> findByUserId(String userId);
    java.util.Optional<LaundryRating> findByLaundryIdAndUserId(String laundryId, String userId);
}
