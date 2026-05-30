package com.laundrify.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.from:noreply@laundrify.com}")
    private String fromEmail;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    public void sendPasswordResetEmail(String toEmail, String resetToken, String userName) {
        try {
            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Request - Laundrify");
            
            String emailBody = String.format(
                "Hello %s,\n\n" +
                "You have requested to reset your password. Please click the link below to proceed:\n\n" +
                "%s\n\n" +
                "This link will expire in 15 minutes.\n\n" +
                "If you did not request a password reset, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Laundrify Team",
                userName,
                resetLink
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to send password reset email: " + e.getMessage());
        }
    }
    
    public void sendWelcomeEmail(String toEmail, String userName, String role) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Laundrify!");
            
            String emailBody = String.format(
                "Hello %s,\n\n" +
                "Welcome to Laundrify! Your account as a %s has been successfully created.\n\n" +
                "You can now log in to your account and start using our services.\n\n" +
                "If you have any questions, feel free to contact us.\n\n" +
                "Best regards,\n" +
                "Laundrify Team",
                userName,
                role.toLowerCase()
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to send welcome email: " + e.getMessage());
        }
    }
}
