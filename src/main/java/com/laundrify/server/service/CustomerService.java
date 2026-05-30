package com.laundrify.server.service;

import com.laundrify.server.dto.CustomerSignupRequest;
import com.laundrify.server.dto.AuthResponse;
import com.laundrify.server.model.Customer;
import com.laundrify.server.model.User;
import com.laundrify.server.repository.CustomerRepository;
import com.laundrify.server.repository.UserRepository;
import com.laundrify.server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {
    
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    
    public AuthResponse signupCustomer(CustomerSignupRequest request) {
        AuthResponse response = new AuthResponse();
        
        try {
            // Validate input
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                response.setSuccess(false);
                response.setMessage("Passwords do not match");
                return response;
            }
            
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                response.setSuccess(false);
                response.setMessage("Email is required");
                return response;
            }
            
            // Check if user already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                response.setSuccess(false);
                response.setMessage("Email already registered");
                return response;
            }
            
            // Create new User
            User user = new User();
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setPhoneNumber(request.getPhoneNumber());
            user.setAddress(request.getAddress());
            user.setCity(request.getCity());
            user.setDistrict(request.getDistrict());
            user.setPostalCode(request.getPostalCode());
            user.setProvince(request.getProvince());
            user.setRole("CUSTOMER");
            user.setEnabled(true);
            user.setCreatedAt(System.currentTimeMillis());
            user.setUpdatedAt(System.currentTimeMillis());
            
            User savedUser = userRepository.save(user);
            
            // Create Customer Profile
            Customer customer = new Customer();
            customer.setUserId(savedUser.getId());
            customer.setFirstName(request.getFirstName());
            customer.setLastName(request.getLastName());
            customer.setEmail(request.getEmail());
            customer.setPhoneNumber(request.getPhoneNumber());
            customer.setAddress(request.getAddress());
            customer.setCity(request.getCity());
            customer.setDistrict(request.getDistrict());
            customer.setPostalCode(request.getPostalCode());
            customer.setProvince(request.getProvince());
            customer.setVerified(false);
            customer.setCreatedAt(System.currentTimeMillis());
            customer.setUpdatedAt(System.currentTimeMillis());
            
            customerRepository.save(customer);
            
            // Generate tokens
            String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getId(), savedUser.getRole());
            String refreshToken = jwtUtil.generateRefreshToken(savedUser.getEmail(), savedUser.getId());
            
            // Build response
            response.setSuccess(true);
            response.setMessage("Customer registered successfully");
            response.setToken(token);
            response.setRefreshToken(refreshToken);
            response.setUserId(savedUser.getId());
            response.setEmail(savedUser.getEmail());
            response.setName(savedUser.getFirstName() + " " + savedUser.getLastName());
            response.setRole(savedUser.getRole());
            
            // Send welcome email
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFirstName(), savedUser.getRole());
            
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error during customer registration: " + e.getMessage());
        }
        
        return response;
    }
    
    public Customer getCustomerByUserId(String userId) {
        return customerRepository.findByUserId(userId).orElse(null);
    }
    
    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email).orElse(null);
    }
}
