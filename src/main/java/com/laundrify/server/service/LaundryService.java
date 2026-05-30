package com.laundrify.server.service;

import com.laundrify.server.dto.AuthResponse;
import com.laundrify.server.dto.LaundrySignupRequest;
import com.laundrify.server.dto.LaundryDetailsResponse;
import com.laundrify.server.dto.NearbyLaundryRequest;
import com.laundrify.server.model.Laundry;
import com.laundrify.server.model.User;
import com.laundrify.server.repository.LaundryRepository;
import com.laundrify.server.repository.UserRepository;
import com.laundrify.server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LaundryService {

    private final LaundryRepository laundryRepository;
    private final UserRepository userRepository;
    private final RatingService ratingService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Value("${file.upload.laundry.dir:uploads/laundry}")
    private String uploadDir;

    public AuthResponse signupLaundry(
            LaundrySignupRequest request,
            MultipartFile logo,
            MultipartFile shopImage,
            MultipartFile additionalImage1,
            MultipartFile additionalImage2
    ) {
        AuthResponse response = new AuthResponse();

        try {
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

            if (userRepository.existsByEmail(request.getEmail())) {
                response.setSuccess(false);
                response.setMessage("Email already registered");
                return response;
            }

            Path uploadsPath = Paths.get(uploadDir);
            Files.createDirectories(uploadsPath);

            String logoPath = null;
            String shopImagePath = null;
            List<String> additionalImagePaths = new ArrayList<>();

            if (logo != null && !logo.isEmpty()) {
                logoPath = saveFile(logo, "logo");
            }

            if (shopImage != null && !shopImage.isEmpty()) {
                shopImagePath = saveFile(shopImage, "shop");
            }

            if (additionalImage1 != null && !additionalImage1.isEmpty()) {
                additionalImagePaths.add(saveFile(additionalImage1, "additional1"));
            }

            if (additionalImage2 != null && !additionalImage2.isEmpty()) {
                additionalImagePaths.add(saveFile(additionalImage2, "additional2"));
            }

            User user = new User();
            user.setFirstName(request.getOwnerFirstName() != null ? request.getOwnerFirstName() : request.getLaundryName());
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

            Laundry laundry = new Laundry();
            laundry.setUserId(savedUser.getId());
            laundry.setLaundryName(request.getLaundryName());
            laundry.setLocation(request.getAddress() != null ? request.getAddress() : request.getCity());
            laundry.setBusinessRegistrationNumber(request.getBusinessRegistrationNumber());
            laundry.setTaxId(request.getTaxId());
            laundry.setContactNumber(request.getContactNumber());
            laundry.setAlternateContactNumber(request.getAlternateContactNumber());
            laundry.setContactEmail(request.getEmail());
            laundry.setWebsite(request.getWebsite());
            laundry.setDescription(request.getDescription());
            laundry.setOpeningHours(request.getOpeningHours());
            laundry.setClosingHours(request.getClosingHours());
            laundry.setServicesOffered(request.getServicesOffered());
            laundry.setOwnerFirstName(request.getOwnerFirstName());
            laundry.setOwnerLastName(request.getOwnerLastName());
            laundry.setOwnerEmail(request.getOwnerEmail() != null ? request.getOwnerEmail() : request.getEmail());
            laundry.setAddress(request.getAddress());
            laundry.setCity(request.getCity());
            laundry.setDistrict(request.getDistrict());
            laundry.setPostalCode(request.getPostalCode());
            laundry.setProvince(request.getProvince());
            laundry.setLogoPath(logoPath);
            laundry.setShopImagePath(shopImagePath);
            laundry.setAdditionalImagePaths(additionalImagePaths);
            laundry.setVerified(false);
            laundry.setCreatedAt(System.currentTimeMillis());
            laundry.setUpdatedAt(System.currentTimeMillis());

            Laundry savedLaundry = laundryRepository.save(laundry);

            response.setSuccess(true);
            response.setMessage("Laundry registration completed successfully");
            response.setUserId(savedUser.getId());
            response.setEmail(savedUser.getEmail());
            response.setName(savedUser.getFirstName() + " " + (savedUser.getLastName() != null ? savedUser.getLastName() : ""));
            response.setRole(savedUser.getRole());
            response.setToken(jwtUtil.generateToken(savedUser.getEmail(), savedUser.getId(), savedUser.getRole()));
            response.setRefreshToken(jwtUtil.generateRefreshToken(savedUser.getEmail(), savedUser.getId()));

            try {
                emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFirstName(), savedUser.getRole());
            } catch (Exception emailException) {
                log.warn("Laundry signup completed but welcome email failed for {}: {}", savedUser.getEmail(), emailException.getMessage());
                response.setMessage("Laundry registration completed successfully (welcome email pending)");
            }

        } catch (IOException e) {
            response.setSuccess(false);
            response.setMessage("Error uploading files: " + e.getMessage());
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error during laundry registration: " + e.getMessage());
        }

        return response;
    }

    private String saveFile(MultipartFile file, String type) throws IOException {
        String fileName = type + "_" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, file.getBytes());
        return filePath.toString();
    }

    public Laundry getLaundryByUserId(String userId) {
        return laundryRepository.findByUserId(userId).orElse(null);
    }

    // Find nearby laundries based on location hierarchy (City > District > Province)
    public List<LaundryDetailsResponse> getNearbyLaundries(NearbyLaundryRequest request) {
        List<Laundry> laundries = new ArrayList<>();
        
        // Search by: City + District + Province (most specific)
        if (request.getCity() != null && request.getDistrict() != null && request.getProvince() != null) {
            laundries = laundryRepository.findByCityAndDistrictAndProvince(
                    request.getCity(),
                    request.getDistrict(),
                    request.getProvince()
            );
        }
        // If no results, search by: City + District
        if (laundries.isEmpty() && request.getCity() != null && request.getDistrict() != null) {
            laundries = laundryRepository.findByCityAndDistrict(
                    request.getCity(),
                    request.getDistrict()
            );
        }
        // If still no results, search by: City
        if (laundries.isEmpty() && request.getCity() != null) {
            laundries = laundryRepository.findByCity(request.getCity());
        }
        // If still no results, search by: Province
        if (laundries.isEmpty() && request.getProvince() != null) {
            laundries = laundryRepository.findByProvince(request.getProvince());
        }
        
        // Convert to response with ratings
        List<LaundryDetailsResponse> responses = new ArrayList<>();
        for (Laundry laundry : laundries) {
            responses.add(convertToDetailsResponse(laundry));
        }
        
        return responses;
    }

    // Get single laundry details with rating
    public LaundryDetailsResponse getLaundryDetails(String laundryId) {
        Optional<Laundry> laundryOptional = laundryRepository.findById(laundryId);
        if (laundryOptional.isPresent()) {
            return convertToDetailsResponse(laundryOptional.get());
        }
        return null;
    }

    // Convert Laundry to LaundryDetailsResponse with ratings
    private LaundryDetailsResponse convertToDetailsResponse(Laundry laundry) {
        LaundryDetailsResponse response = new LaundryDetailsResponse();
        response.setId(laundry.getId());
        response.setLaundryName(laundry.getLaundryName());
        response.setAddress(laundry.getAddress());
        response.setCity(laundry.getCity());
        response.setDistrict(laundry.getDistrict());
        response.setProvince(laundry.getProvince());
        response.setContactNumber(laundry.getContactNumber());
        response.setAlternateContactNumber(laundry.getAlternateContactNumber());
        response.setWebsite(laundry.getWebsite());
        response.setDescription(laundry.getDescription());
        response.setServicesOffered(laundry.getServicesOffered());
        response.setOpeningHours(laundry.getOpeningHours());
        response.setClosingHours(laundry.getClosingHours());
        response.setLogoPath(laundry.getLogoPath());
        response.setShopImagePath(laundry.getShopImagePath());
        response.setVerified(laundry.isVerified());
        
        // Add rating information
        double avgRating = ratingService.getAverageLaundryRating(laundry.getId());
        response.setAverageRating(avgRating);
        response.setTotalRatings(ratingService.getLaundryRatings(laundry.getId()).size());
        
        return response;
    }
}
