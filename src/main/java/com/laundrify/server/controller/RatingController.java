package com.laundrify.server.controller;

import com.laundrify.server.dto.LaundryRatingRequest;
import com.laundrify.server.dto.DriverRatingRequest;
import com.laundrify.server.dto.AuthResponse;
import com.laundrify.server.dto.NearbyLaundryRequest;
import com.laundrify.server.dto.LaundryDetailsResponse;
import com.laundrify.server.model.LaundryRating;
import com.laundrify.server.model.DriverRating;
import com.laundrify.server.service.RatingService;
import com.laundrify.server.service.LaundryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class RatingController {
    
    private final RatingService ratingService;
    private final LaundryService laundryService;
    
    // ============== Laundry Ratings ==============
    
    @PostMapping("/laundry")
    public ResponseEntity<AuthResponse> addLaundryRating(
            @RequestParam String userId,
            @RequestBody LaundryRatingRequest request) {
        
        AuthResponse response = ratingService.addLaundryRating(userId, request);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @GetMapping("/laundry/{laundryId}")
    public ResponseEntity<?> getLaundryRatings(@PathVariable String laundryId) {
        try {
            List<LaundryRating> ratings = ratingService.getLaundryRatings(laundryId);
            double averageRating = ratingService.getAverageLaundryRating(laundryId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("laundryId", laundryId);
            response.put("ratings", ratings);
            response.put("averageRating", averageRating);
            response.put("totalRatings", ratings.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new HashMap<String, String>() {{
                        put("error", "Failed to fetch laundry ratings: " + e.getMessage());
                    }});
        }
    }
    
    // ============== Driver Ratings ==============
    
    @PostMapping("/driver")
    public ResponseEntity<AuthResponse> addDriverRating(
            @RequestParam String userId,
            @RequestBody DriverRatingRequest request) {
        
        AuthResponse response = ratingService.addDriverRating(userId, request);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<?> getDriverRatings(@PathVariable String driverId) {
        try {
            List<DriverRating> ratings = ratingService.getDriverRatings(driverId);
            double averageRating = ratingService.getAverageDriverRating(driverId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("driverId", driverId);
            response.put("ratings", ratings);
            response.put("averageRating", averageRating);
            response.put("totalRatings", ratings.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new HashMap<String, String>() {{
                        put("error", "Failed to fetch driver ratings: " + e.getMessage());
                    }});
        }
    }
    
    // ============== Nearby Laundries ==============
    
    @PostMapping("/laundries/nearby")
    public ResponseEntity<?> getNearbyLaundries(@RequestBody NearbyLaundryRequest request) {
        try {
            List<LaundryDetailsResponse> laundries = laundryService.getNearbyLaundries(request);
            
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("laundries", laundries);
                put("count", laundries.size());
            }});
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new HashMap<String, String>() {{
                        put("error", "Failed to fetch nearby laundries: " + e.getMessage());
                    }});
        }
    }
    
    @GetMapping("/laundries/nearby")
    public ResponseEntity<?> getNearbyLaundriesByParams(
            @RequestParam String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String province,
            @RequestParam(required = false, defaultValue = "50") int radiusKm) {
        
        try {
            NearbyLaundryRequest request = new NearbyLaundryRequest();
            request.setCity(city);
            request.setDistrict(district);
            request.setProvince(province);
            request.setRadiusKm(radiusKm);
            
            List<LaundryDetailsResponse> laundries = laundryService.getNearbyLaundries(request);
            
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("laundries", laundries);
                put("count", laundries.size());
                put("searchCriteria", new HashMap<String, Object>() {{
                    put("city", city);
                    put("district", district);
                    put("province", province);
                    put("radiusKm", radiusKm);
                }});
            }});
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new HashMap<String, String>() {{
                        put("error", "Failed to fetch nearby laundries: " + e.getMessage());
                    }});
        }
    }

    @GetMapping("/laundries/search")
    public ResponseEntity<?> searchLaundries(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String location) {
        try {
            List<LaundryDetailsResponse> laundries = laundryService.searchLaundriesByText(query, location);
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("laundries", laundries);
                put("count", laundries.size());
            }});
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new HashMap<String, String>() {{
                        put("error", "Search failed: " + e.getMessage());
                    }});
        }
    }
    
    @GetMapping("/laundries/{laundryId}")
    public ResponseEntity<?> getLaundryDetails(@PathVariable String laundryId) {
        try {
            LaundryDetailsResponse laundry = laundryService.getLaundryDetails(laundryId);
            
            if (laundry == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new HashMap<String, String>() {{
                            put("error", "Laundry not found");
                        }});
            }
            
            return ResponseEntity.ok(laundry);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new HashMap<String, String>() {{
                        put("error", "Failed to fetch laundry details: " + e.getMessage());
                    }});
        }
    }
}
