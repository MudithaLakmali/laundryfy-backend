package com.laundrify.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "password_reset_tokens")
public class PasswordResetToken {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String token;
    
    private String email;
    private String userId;
    
    private long createdAt;
    private long expiryTime; // Token expiration time (15 minutes)
    private boolean used;
}
