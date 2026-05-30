package com.laundrify.server.service;

import com.laundrify.server.dto.SignupRequest;
import com.laundrify.server.dto.LoginRequest;
import com.laundrify.server.dto.AuthResponse;
import com.laundrify.server.dto.PasswordResetRequest;
import com.laundrify.server.dto.PasswordResetVerifyRequest;
import com.laundrify.server.dto.DriverSignupRequest;
import com.laundrify.server.dto.LaundrySignupRequest;
import com.laundrify.server.model.User;
import com.laundrify.server.model.PasswordResetToken;
import com.laundrify.server.repository.UserRepository;
import com.laundrify.server.repository.PasswordResetTokenRepository;
import com.laundrify.server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private static final long TOKEN_EXPIRY_TIME = 15 * 60 * 1000; // 15 minutes in milliseconds
    
    public AuthResponse signup(SignupRequest request) {
        AuthResponse response = new AuthResponse();
        
        // Validate input
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            response.setSuccess(false);
            response.setMessage("Passwords do not match");
            return response;
        }
        
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            response.setSuccess(false);
            response.setMessage("Email already registered");
            return response;
        }
        
        try {
            // Create new user
            User user = new User();
            user.setFirstName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(request.getRole() != null ? request.getRole() : "CUSTOMER");
            user.setEnabled(true);
            user.setCreatedAt(System.currentTimeMillis());
            user.setUpdatedAt(System.currentTimeMillis());
            
            // Save user to database
            User savedUser = userRepository.save(user);
            
            // Generate tokens
            String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getId(), savedUser.getRole());
            String refreshToken = jwtUtil.generateRefreshToken(savedUser.getEmail(), savedUser.getId());
            
            // Build response
            response.setSuccess(true);
            response.setMessage("User registered successfully");
            response.setToken(token);
            response.setRefreshToken(refreshToken);
            response.setUserId(savedUser.getId());
            response.setEmail(savedUser.getEmail());
            response.setName(savedUser.getFirstName());
            response.setRole(savedUser.getRole());
            
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error during registration: " + e.getMessage());
        }
        
        return response;
    }
    
    public AuthResponse signupDriver(DriverSignupRequest request) {
        AuthResponse response = new AuthResponse();
        
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
        
        try {
            // Create new user with DRIVER role
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
            user.setRole("DRIVER");
            user.setEnabled(true);
            user.setCreatedAt(System.currentTimeMillis());
            user.setUpdatedAt(System.currentTimeMillis());
            
            User savedUser = userRepository.save(user);
            
            // Generate tokens
            String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getId(), savedUser.getRole());
            String refreshToken = jwtUtil.generateRefreshToken(savedUser.getEmail(), savedUser.getId());
            
            // Build response
            response.setSuccess(true);
            response.setMessage("Driver registered successfully. Please complete driver details.");
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
            response.setMessage("Error during driver registration: " + e.getMessage());
        }
        
        return response;
    }
    
    public AuthResponse signupLaundry(LaundrySignupRequest request) {
        AuthResponse response = new AuthResponse();
        
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
        
        try {
            // Create new user with LAUNDRY role
            User user = new User();
            user.setFirstName(request.getOwnerFirstName());
            user.setLastName(request.getOwnerLastName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setPhoneNumber(request.getContactNumber());
            user.setAddress(request.getAddress());
            user.setCity(request.getCity());
            user.setDistrict(request.getDistrict());
            user.setPostalCode(request.getPostalCode());
            user.setProvince(request.getProvince());
            user.setRole("LAUNDRY");
            user.setEnabled(true);
            user.setCreatedAt(System.currentTimeMillis());
            user.setUpdatedAt(System.currentTimeMillis());
            
            User savedUser = userRepository.save(user);
            
            // Generate tokens
            String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getId(), savedUser.getRole());
            String refreshToken = jwtUtil.generateRefreshToken(savedUser.getEmail(), savedUser.getId());
            
            // Build response
            response.setSuccess(true);
            response.setMessage("Laundry registered successfully. Please complete laundry details.");
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
            response.setMessage("Error during laundry registration: " + e.getMessage());
        }
        
        return response;
    }
    
    // Unified login for all user types
    public AuthResponse login(LoginRequest request) {
        AuthResponse response = new AuthResponse();
        
        try {
            // Find user by email (works for all user types)
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
            
            if (userOptional.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("Invalid email or password");
                return response;
            }
            
            User user = userOptional.get();
            
            // Validate password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                response.setSuccess(false);
                response.setMessage("Invalid email or password");
                return response;
            }
            
            // Check if user is enabled
            if (!user.isEnabled()) {
                response.setSuccess(false);
                response.setMessage("User account is disabled");
                return response;
            }
            
            // Generate tokens
            String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole());
            String refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getId());
            
            // Build response
            response.setSuccess(true);
            response.setMessage("Login successful");
            response.setToken(token);
            response.setRefreshToken(refreshToken);
            response.setUserId(user.getId());
            response.setEmail(user.getEmail());
            response.setName(user.getFirstName() != null ? user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : "") : "User");
            response.setRole(user.getRole());
            
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error during login: " + e.getMessage());
        }
        
        return response;
    }
    
    // Password Reset - Step 1: Request password reset
    public AuthResponse requestPasswordReset(PasswordResetRequest request) {
        AuthResponse response = new AuthResponse();
        
        try {
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
            
            if (userOptional.isEmpty()) {
                // Don't reveal if email exists for security reasons
                response.setSuccess(true);
                response.setMessage("If the email exists, a password reset link has been sent");
                return response;
            }
            
            User user = userOptional.get();
            
            // Delete any existing reset tokens for this email
            passwordResetTokenRepository.deleteByEmail(request.getEmail());
            
            // Generate reset token
            String resetToken = UUID.randomUUID().toString();
            long createdAt = System.currentTimeMillis();
            long expiryTime = createdAt + TOKEN_EXPIRY_TIME;
            
            PasswordResetToken token = new PasswordResetToken();
            token.setToken(resetToken);
            token.setEmail(request.getEmail());
            token.setUserId(user.getId());
            token.setCreatedAt(createdAt);
            token.setExpiryTime(expiryTime);
            token.setUsed(false);
            
            passwordResetTokenRepository.save(token);
            
            // Send password reset email
            emailService.sendPasswordResetEmail(request.getEmail(), resetToken, user.getFirstName());
            
            response.setSuccess(true);
            response.setMessage("If the email exists, a password reset link has been sent");
            
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error processing password reset request: " + e.getMessage());
        }
        
        return response;
    }
    
    // Password Reset - Step 2: Verify token and reset password
    public AuthResponse resetPassword(PasswordResetVerifyRequest request) {
        AuthResponse response = new AuthResponse();
        
        try {
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                response.setSuccess(false);
                response.setMessage("Passwords do not match");
                return response;
            }
            
            Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(request.getToken());
            
            if (tokenOptional.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("Invalid or expired reset token");
                return response;
            }
            
            PasswordResetToken resetToken = tokenOptional.get();
            
            // Check if token has expired
            if (System.currentTimeMillis() > resetToken.getExpiryTime()) {
                response.setSuccess(false);
                response.setMessage("Reset token has expired");
                return response;
            }
            
            // Check if token has already been used
            if (resetToken.isUsed()) {
                response.setSuccess(false);
                response.setMessage("This reset token has already been used");
                return response;
            }
            
            // Find user and update password
            Optional<User> userOptional = userRepository.findById(resetToken.getUserId());
            
            if (userOptional.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("User not found");
                return response;
            }
            
            User user = userOptional.get();
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.setUpdatedAt(System.currentTimeMillis());
            userRepository.save(user);
            
            // Mark token as used
            resetToken.setUsed(true);
            passwordResetTokenRepository.save(resetToken);
            
            response.setSuccess(true);
            response.setMessage("Password has been reset successfully. Please login with your new password.");
            
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error resetting password: " + e.getMessage());
        }
        
        return response;
    }
    
    public User getUserById(String userId) {
        return userRepository.findById(userId).orElse(null);
    }
    
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
