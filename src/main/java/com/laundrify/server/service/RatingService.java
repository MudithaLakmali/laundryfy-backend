package com.laundrify.server.service;

import com.laundrify.server.dto.LaundryRatingRequest;
import com.laundrify.server.dto.DriverRatingRequest;
import com.laundrify.server.dto.AuthResponse;
import com.laundrify.server.model.LaundryRating;
import com.laundrify.server.model.DriverRating;
import com.laundrify.server.model.User;
import com.laundrify.server.repository.LaundryRatingRepository;
import com.laundrify.server.repository.DriverRatingRepository;
import com.laundrify.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RatingService {
    
    private final LaundryRatingRepository laundryRatingRepository;
    private final DriverRatingRepository driverRatingRepository;
    private final UserRepository userRepository;
    
    // Laundry Rating Methods
    
    public AuthResponse addLaundryRating(String userId, LaundryRatingRequest request) {
        AuthResponse response = new AuthResponse();
        
        try {
            // Validate rating
            if (request.getRating() < 1 || request.getRating() > 5) {
                response.setSuccess(false);
                response.setMessage("Rating must be between 1 and 5");
                return response;
            }
            
            // Get user details
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("User not found");
                return response;
            }
            
            User user = userOptional.get();
            
            // Check if user already rated this laundry
            Optional<LaundryRating> existingRating = laundryRatingRepository.findByLaundryIdAndUserId(
                    request.getLaundryId(), userId);
            
            LaundryRating rating;
            if (existingRating.isPresent()) {
                // Update existing rating
                rating = existingRating.get();
                rating.setRating(request.getRating());
                rating.setReview(request.getReview());
                rating.setImages(request.getImages());
                rating.setUpdatedAt(System.currentTimeMillis());
                response.setMessage("Rating updated successfully");
            } else {
                // Create new rating
                rating = new LaundryRating();
                rating.setLaundryId(request.getLaundryId());
                rating.setUserId(userId);
                rating.setCustomerName(user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : ""));
                rating.setRating(request.getRating());
                rating.setReview(request.getReview());
                rating.setImages(request.getImages());
                rating.setCreatedAt(System.currentTimeMillis());
                rating.setUpdatedAt(System.currentTimeMillis());
                response.setMessage("Rating added successfully");
            }
            
            LaundryRating savedRating = laundryRatingRepository.save(rating);
            response.setSuccess(true);
            response.setUserId(savedRating.getId());
            
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error adding laundry rating: " + e.getMessage());
        }
        
        return response;
    }
    
    public List<LaundryRating> getLaundryRatings(String laundryId) {
        return laundryRatingRepository.findByLaundryId(laundryId);
    }
    
    public double getAverageLaundryRating(String laundryId) {
        List<LaundryRating> ratings = laundryRatingRepository.findByLaundryId(laundryId);
        if (ratings.isEmpty()) {
            return 0.0;
        }
        return ratings.stream()
                .mapToDouble(LaundryRating::getRating)
                .average()
                .orElse(0.0);
    }
    
    // Driver Rating Methods
    
    public AuthResponse addDriverRating(String userId, DriverRatingRequest request) {
        AuthResponse response = new AuthResponse();
        
        try {
            // Validate rating
            if (request.getRating() < 1 || request.getRating() > 5) {
                response.setSuccess(false);
                response.setMessage("Rating must be between 1 and 5");
                return response;
            }
            
            // Get user details
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("User not found");
                return response;
            }
            
            User user = userOptional.get();
            
            // Check if user already rated this driver
            Optional<DriverRating> existingRating = driverRatingRepository.findByDriverIdAndUserId(
                    request.getDriverId(), userId);
            
            DriverRating rating;
            if (existingRating.isPresent()) {
                // Update existing rating
                rating = existingRating.get();
                rating.setRating(request.getRating());
                rating.setReview(request.getReview());
                rating.setImages(request.getImages());
                rating.setUpdatedAt(System.currentTimeMillis());
                response.setMessage("Rating updated successfully");
            } else {
                // Create new rating
                rating = new DriverRating();
                rating.setDriverId(request.getDriverId());
                rating.setUserId(userId);
                rating.setCustomerName(user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : ""));
                rating.setRating(request.getRating());
                rating.setReview(request.getReview());
                rating.setImages(request.getImages());
                rating.setCreatedAt(System.currentTimeMillis());
                rating.setUpdatedAt(System.currentTimeMillis());
                response.setMessage("Rating added successfully");
            }
            
            DriverRating savedRating = driverRatingRepository.save(rating);
            response.setSuccess(true);
            response.setUserId(savedRating.getId());
            
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error adding driver rating: " + e.getMessage());
        }
        
        return response;
    }
    
    public List<DriverRating> getDriverRatings(String driverId) {
        return driverRatingRepository.findByDriverId(driverId);
    }
    
    public double getAverageDriverRating(String driverId) {
        List<DriverRating> ratings = driverRatingRepository.findByDriverId(driverId);
        if (ratings.isEmpty()) {
            return 0.0;
        }
        return ratings.stream()
                .mapToDouble(DriverRating::getRating)
                .average()
                .orElse(0.0);
    }
}
