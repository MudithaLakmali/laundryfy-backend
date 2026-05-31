package com.laundrify.server.controller;

import com.laundrify.server.model.Driver;
import com.laundrify.server.model.Laundry;
import com.laundrify.server.model.User;
import com.laundrify.server.model.DriverRating;
import com.laundrify.server.model.LaundryRating;
import com.laundrify.server.repository.DriverRepository;
import com.laundrify.server.repository.LaundryRepository;
import com.laundrify.server.repository.UserRepository;
import com.laundrify.server.repository.DriverRatingRepository;
import com.laundrify.server.repository.LaundryRatingRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    private final LaundryRepository laundryRepository;
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final LaundryRatingRepository laundryRatingRepository;
    private final DriverRatingRepository driverRatingRepository;

    // 1. Fetch all unverified registrations
    @GetMapping("/registrations")
    public ResponseEntity<?> getPendingRegistrations() {
        List<Laundry> pendingLaundries = laundryRepository.findAll().stream()
                .filter(l -> !l.isVerified())
                .collect(Collectors.toList());

        List<Driver> pendingDrivers = driverRepository.findAll().stream()
                .filter(d -> !d.isVerified())
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("laundries", pendingLaundries);
        response.put("drivers", pendingDrivers);

        return ResponseEntity.ok(response);
    }

    // 2. Approve Laundry
    @PostMapping("/laundries/{laundryId}/approve")
    public ResponseEntity<?> approveLaundry(@PathVariable String laundryId) {
        Optional<Laundry> laundryOpt = laundryRepository.findById(laundryId);
        if (laundryOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Laundry not found"));
        }

        Laundry laundry = laundryOpt.get();
        laundry.setVerified(true);
        laundry.setBanned(false);
        laundryRepository.save(laundry);

        if (laundry.getUserId() != null) {
            Optional<User> userOpt = userRepository.findById(laundry.getUserId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setEnabled(true);
                userRepository.save(user);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Laundry registration approved successfully");
        return ResponseEntity.ok(response);
    }

    // 3. Reject Laundry (Delete request)
    @PostMapping("/laundries/{laundryId}/reject")
    public ResponseEntity<?> rejectLaundry(@PathVariable String laundryId) {
        Optional<Laundry> laundryOpt = laundryRepository.findById(laundryId);
        if (laundryOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Laundry not found"));
        }

        Laundry laundry = laundryOpt.get();
        if (laundry.getUserId() != null) {
            userRepository.deleteById(laundry.getUserId());
        }
        laundryRepository.deleteById(laundryId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Laundry registration rejected and deleted");
        return ResponseEntity.ok(response);
    }

    // 4. Approve Driver
    @PostMapping("/drivers/{driverId}/approve")
    public ResponseEntity<?> approveDriver(@PathVariable String driverId) {
        Optional<Driver> driverOpt = driverRepository.findById(driverId);
        if (driverOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Driver not found"));
        }

        Driver driver = driverOpt.get();
        driver.setVerified(true);
        driver.setBanned(false);
        driverRepository.save(driver);

        if (driver.getUserId() != null) {
            Optional<User> userOpt = userRepository.findById(driver.getUserId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setEnabled(true);
                userRepository.save(user);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Driver registration approved successfully");
        return ResponseEntity.ok(response);
    }

    // 5. Reject Driver (Delete request)
    @PostMapping("/drivers/{driverId}/reject")
    public ResponseEntity<?> rejectDriver(@PathVariable String driverId) {
        Optional<Driver> driverOpt = driverRepository.findById(driverId);
        if (driverOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Driver not found"));
        }

        Driver driver = driverOpt.get();
        if (driver.getUserId() != null) {
            userRepository.deleteById(driver.getUserId());
        }
        driverRepository.deleteById(driverId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Driver registration rejected and deleted");
        return ResponseEntity.ok(response);
    }

    // 6. Get all Laundries with ratings/reviews (for management / moderation)
    @GetMapping("/laundries")
    public ResponseEntity<?> getAllLaundriesForModeration() {
        List<Laundry> laundries = laundryRepository.findAll();
        List<Map<String, Object>> list = new ArrayList<>();

        for (Laundry laundry : laundries) {
            List<LaundryRating> reviews = laundryRatingRepository.findByLaundryId(laundry.getId());
            double avgRating = reviews.isEmpty() ? 0.0 : reviews.stream().mapToDouble(LaundryRating::getRating).average().orElse(0.0);

            Map<String, Object> map = new HashMap<>();
            map.put("laundry", laundry);
            map.put("averageRating", avgRating);
            map.put("totalRatings", reviews.size());
            map.put("reviews", reviews);
            list.add(map);
        }

        return ResponseEntity.ok(list);
    }

    // 7. Get all Drivers with ratings/reviews (for management / moderation)
    @GetMapping("/drivers")
    public ResponseEntity<?> getAllDriversForModeration() {
        List<Driver> drivers = driverRepository.findAll();
        List<Map<String, Object>> list = new ArrayList<>();

        for (Driver driver : drivers) {
            List<DriverRating> reviews = driverRatingRepository.findByDriverId(driver.getId());
            double avgRating = reviews.isEmpty() ? 0.0 : reviews.stream().mapToDouble(DriverRating::getRating).average().orElse(0.0);

            Map<String, Object> map = new HashMap<>();
            map.put("driver", driver);
            map.put("averageRating", avgRating);
            map.put("totalRatings", reviews.size());
            map.put("reviews", reviews);
            list.add(map);
        }

        return ResponseEntity.ok(list);
    }

    // 8. Ban Laundry
    @PostMapping("/laundries/{laundryId}/ban")
    public ResponseEntity<?> banLaundry(@PathVariable String laundryId) {
        Optional<Laundry> laundryOpt = laundryRepository.findById(laundryId);
        if (laundryOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Laundry not found"));
        }

        Laundry laundry = laundryOpt.get();
        laundry.setBanned(true);
        laundryRepository.save(laundry);

        if (laundry.getUserId() != null) {
            Optional<User> userOpt = userRepository.findById(laundry.getUserId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setEnabled(false);
                userRepository.save(user);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Laundry banned successfully");
        return ResponseEntity.ok(response);
    }

    // 9. Unban Laundry
    @PostMapping("/laundries/{laundryId}/unban")
    public ResponseEntity<?> unbanLaundry(@PathVariable String laundryId) {
        Optional<Laundry> laundryOpt = laundryRepository.findById(laundryId);
        if (laundryOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Laundry not found"));
        }

        Laundry laundry = laundryOpt.get();
        laundry.setBanned(false);
        laundryRepository.save(laundry);

        if (laundry.getUserId() != null) {
            Optional<User> userOpt = userRepository.findById(laundry.getUserId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setEnabled(true);
                userRepository.save(user);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Laundry unbanned successfully");
        return ResponseEntity.ok(response);
    }

    // 10. Ban Driver
    @PostMapping("/drivers/{driverId}/ban")
    public ResponseEntity<?> banDriver(@PathVariable String driverId) {
        Optional<Driver> driverOpt = driverRepository.findById(driverId);
        if (driverOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Driver not found"));
        }

        Driver driver = driverOpt.get();
        driver.setBanned(true);
        driverRepository.save(driver);

        if (driver.getUserId() != null) {
            Optional<User> userOpt = userRepository.findById(driver.getUserId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setEnabled(false);
                userRepository.save(user);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Driver banned successfully");
        return ResponseEntity.ok(response);
    }

    // 11. Unban Driver
    @PostMapping("/drivers/{driverId}/unban")
    public ResponseEntity<?> unbanDriver(@PathVariable String driverId) {
        Optional<Driver> driverOpt = driverRepository.findById(driverId);
        if (driverOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Driver not found"));
        }

        Driver driver = driverOpt.get();
        driver.setBanned(false);
        driverRepository.save(driver);

        if (driver.getUserId() != null) {
            Optional<User> userOpt = userRepository.findById(driver.getUserId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setEnabled(true);
                userRepository.save(user);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Driver unbanned successfully");
        return ResponseEntity.ok(response);
    }

    static class ErrorResponse {
        public String message;
        public boolean success = false;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}
