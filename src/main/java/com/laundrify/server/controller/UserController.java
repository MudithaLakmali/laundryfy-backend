
package com.laundrify.server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.laundrify.server.dto.UserResponse;
import com.laundrify.server.model.User;
import com.laundrify.server.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private final AuthService authService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable String userId) {
        User user = authService.getUserById(userId);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse("User not found for id: " + userId)
            );
        }

        UserResponse response = new UserResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getRole(),
            user.isEnabled(),
            user.getAddress(),
            user.getCity(),
            user.getDistrict(),
            user.getProvince(),
            user.getPostalCode()
        );

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
