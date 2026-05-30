package com.laundrify.server.controller;

import com.laundrify.server.model.Driver;
import com.laundrify.server.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class DriverController {
    
    private final DriverService driverService;
    
    @GetMapping("/{userId}")
    public ResponseEntity<?> getDriverByUserId(@PathVariable String userId) {
        Driver driver = driverService.getDriverByUserId(userId);
        
        if (driver == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse("Driver not found for user: " + userId)
            );
        }
        
        return ResponseEntity.status(HttpStatus.OK).body(driver);
    }
    
    @GetMapping("/id/{driverId}")
    public ResponseEntity<?> getDriverById(@PathVariable String driverId) {
        Driver driver = driverService.getDriverById(driverId);
        
        if (driver == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse("Driver not found with id: " + driverId)
            );
        }
        
        return ResponseEntity.status(HttpStatus.OK).body(driver);
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
