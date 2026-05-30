package com.laundrify.server.controller;

import com.laundrify.server.model.Laundry;
import com.laundrify.server.service.LaundryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/laundry")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class LaundryController {

    private final LaundryService laundryService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getLaundryByUserId(@PathVariable String userId) {
        Laundry laundry = laundryService.getLaundryByUserId(userId);

        if (laundry == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse("Laundry profile not found for user: " + userId)
            );
        }

        return ResponseEntity.ok(laundry);
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
