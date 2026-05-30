package com.laundrify.server.service;

import com.laundrify.server.dto.AuthResponse;
import com.laundrify.server.dto.DriverSignupRequest;
import com.laundrify.server.model.Driver;
import com.laundrify.server.model.User;
import com.laundrify.server.repository.DriverRepository;
import com.laundrify.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DriverService {
    
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${file.upload.dir:uploads/drivers}")
    private String uploadDir;

    public AuthResponse signupDriver(DriverSignupRequest request, MultipartFile selfieImage, MultipartFile vehicleNumberPlateImage) {
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

            if (selfieImage == null || selfieImage.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("Selfie image is required");
                return response;
            }

            if (vehicleNumberPlateImage == null || vehicleNumberPlateImage.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("Vehicle number plate image is required");
                return response;
            }

            if (userRepository.existsByEmail(request.getEmail())) {
                response.setSuccess(false);
                response.setMessage("Email already registered");
                return response;
            }

            Path uploadsPath = Paths.get(uploadDir);
            Files.createDirectories(uploadsPath);

            String selfieImagePath = saveFile(selfieImage, "selfie");
            String vehicleNumberPlateImagePath = saveFile(vehicleNumberPlateImage, "vehicle_plate");

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

            Driver driver = new Driver();
            driver.setUserId(savedUser.getId());
            driver.setFirstName(request.getFirstName());
            driver.setLastName(request.getLastName());
            driver.setEmail(request.getEmail());
            driver.setPhoneNumber(request.getPhoneNumber());
            driver.setLicenseNumber(request.getLicenseNumber());
            driver.setLicenseExpiryDate(request.getLicenseExpiryDate());
            driver.setVehicleType(request.getVehicleType());
            driver.setVehicleModel(request.getVehicleModel());
            driver.setVehicleNumber(request.getVehicleNumber());
            driver.setVehicleRegistrationNumber(request.getVehicleRegistrationNumber());
            driver.setVehicleInsuranceNumber(request.getVehicleInsuranceNumber());
            driver.setAddress(request.getAddress());
            driver.setCity(request.getCity());
            driver.setDistrict(request.getDistrict());
            driver.setPostalCode(request.getPostalCode());
            driver.setProvince(request.getProvince());
            driver.setProfileImagePath(selfieImagePath);
            driver.setVehicleImagePath(vehicleNumberPlateImagePath);
            driver.setVerified(false);
            driver.setCreatedAt(System.currentTimeMillis());
            driver.setUpdatedAt(System.currentTimeMillis());

            driverRepository.save(driver);

            response.setSuccess(true);
            response.setMessage("Driver registered successfully");
            response.setUserId(savedUser.getId());
            response.setEmail(savedUser.getEmail());
            response.setName(savedUser.getFirstName() + " " + savedUser.getLastName());
            response.setRole(savedUser.getRole());

        } catch (IOException e) {
            response.setSuccess(false);
            response.setMessage("Error uploading files: " + e.getMessage());
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error during driver registration: " + e.getMessage());
        }

        return response;
    }
    
    public AuthResponse completeDriverRegistration(String userId, String licenseNumber, String vehicleType,
                                                   String vehicleModel, String vehicleNumber, String phoneNumber,
                                                   String address, MultipartFile licenseImage,
                                                   MultipartFile vehicleImage, MultipartFile profileImage) {
        
        AuthResponse response = new AuthResponse();
        
        try {
            // Check if user exists
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("User not found");
                return response;
            }
            
            // Check if driver already registered
            if (driverRepository.existsByUserId(userId)) {
                response.setSuccess(false);
                response.setMessage("Driver already registered for this user");
                return response;
            }
            
            // Create uploads directory if it doesn't exist
            Path uploadsPath = Paths.get(uploadDir);
            Files.createDirectories(uploadsPath);
            
            // Save images
            String licenseImagePath = null;
            String vehicleImagePath = null;
            String profileImagePath = null;
            
            if (licenseImage != null && !licenseImage.isEmpty()) {
                licenseImagePath = saveFile(licenseImage, "license");
            }
            
            if (vehicleImage != null && !vehicleImage.isEmpty()) {
                vehicleImagePath = saveFile(vehicleImage, "vehicle");
            }
            
            if (profileImage != null && !profileImage.isEmpty()) {
                profileImagePath = saveFile(profileImage, "profile");
            }
            
            // Create and save driver
            Driver driver = new Driver();
            driver.setUserId(userId);
            driver.setLicenseNumber(licenseNumber);
            driver.setVehicleType(vehicleType);
            driver.setVehicleModel(vehicleModel);
            driver.setVehicleNumber(vehicleNumber);
            driver.setPhoneNumber(phoneNumber);
            driver.setAddress(address);
            driver.setLicenseImagePath(licenseImagePath);
            driver.setVehicleImagePath(vehicleImagePath);
            driver.setProfileImagePath(profileImagePath);
            driver.setVerified(false);
            driver.setCreatedAt(System.currentTimeMillis());
            
            Driver savedDriver = driverRepository.save(driver);
            
            response.setSuccess(true);
            response.setMessage("Driver registration completed successfully");
            response.setUserId(savedDriver.getId());
            
        } catch (IOException e) {
            response.setSuccess(false);
            response.setMessage("Error uploading files: " + e.getMessage());
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error during driver registration: " + e.getMessage());
        }
        
        return response;
    }
    
    private String saveFile(MultipartFile file, String type) throws IOException {
        String fileName = type + "_" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        Files.write(filePath, file.getBytes());
        return filePath.toString();
    }
    
    public Driver getDriverByUserId(String userId) {
        return driverRepository.findByUserId(userId).orElse(null);
    }
    
    public Driver getDriverById(String driverId) {
        return driverRepository.findById(driverId).orElse(null);
    }
}
