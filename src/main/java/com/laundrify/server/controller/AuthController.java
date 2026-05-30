package com.laundrify.server.controller;

import com.laundrify.server.dto.LoginRequest;
import com.laundrify.server.dto.AuthResponse;
import com.laundrify.server.dto.CustomerSignupRequest;
import com.laundrify.server.dto.DriverSignupRequest;
import com.laundrify.server.dto.LaundrySignupRequest;
import com.laundrify.server.dto.PasswordResetRequest;
import com.laundrify.server.dto.PasswordResetVerifyRequest;
import com.laundrify.server.service.AuthService;
import com.laundrify.server.service.CustomerService;
import com.laundrify.server.service.DriverService;
import com.laundrify.server.service.LaundryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private final AuthService authService;
    private final CustomerService customerService;
    private final DriverService driverService;
    private final LaundryService laundryService;

    
    // Customer Signup Endpoint
    @PostMapping("/signup/customer")
    public ResponseEntity<AuthResponse> signupCustomer(@RequestBody CustomerSignupRequest request) {
        AuthResponse response = customerService.signupCustomer(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    // Driver Signup Endpoint
    @PostMapping(value = "/signup/driver", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthResponse> signupDriver(
            @RequestPart("data") DriverSignupRequest request,
            @RequestPart("selfieImage") MultipartFile selfieImage,
            @RequestPart("vehicleNumberPlateImage") MultipartFile vehicleNumberPlateImage) {

        AuthResponse response = driverService.signupDriver(request, selfieImage, vehicleNumberPlateImage);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    // Laundry Signup Endpoint
    @PostMapping(value = "/signup/laundry", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthResponse> signupLaundry(
            @RequestPart("data") LaundrySignupRequest request,
            @RequestPart("logo") MultipartFile logo,
            @RequestPart("shopImage") MultipartFile shopImage,
            @RequestPart(value = "additionalImage1", required = false) MultipartFile additionalImage1,
            @RequestPart(value = "additionalImage2", required = false) MultipartFile additionalImage2) {

        AuthResponse response = laundryService.signupLaundry(
                request,
                logo,
                shopImage,
                additionalImage1,
                additionalImage2);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    // Unified Login Endpoint - works for all user types
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    // Password Reset - Step 1: Request password reset
    @PostMapping("/password-reset/request")
    public ResponseEntity<AuthResponse> requestPasswordReset(@RequestBody PasswordResetRequest request) {
        AuthResponse response = authService.requestPasswordReset(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    // Password Reset - Step 2: Verify token and reset password
    @PostMapping("/password-reset/verify")
    public ResponseEntity<AuthResponse> resetPassword(@RequestBody PasswordResetVerifyRequest request) {
        AuthResponse response = authService.resetPassword(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }
 
}
