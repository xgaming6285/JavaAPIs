package com.example.demo;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service for handling email-related operations.
 * Provides functionality for sending verification and password reset emails.
 */
@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${server.url:http://localhost:8080}")
    private String baseUrl;

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String token) {
        String confirmationUrl = baseUrl + "/api/auth/verify?token=" + token;
        sendEmail(to, "Email Verification", "Please verify your email by clicking the link: " + confirmationUrl);
    }

    public void sendResetPasswordEmail(String to, String token) {
        String resetUrl = baseUrl + "/api/auth/update-password?token=" + token;
        sendEmail(to, "Password Reset Request", "Please reset your password by clicking the link: " + resetUrl);
    }

    private void sendEmail(String to, String subject, String message) {
        validateEmail(to);

        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(fromEmail);
        email.setTo(to);
        email.setSubject(subject);
        email.setText(message);
        
        try {
            mailSender.send(email);
            logger.info("Email sent successfully to {}", to);
        } catch (MailAuthenticationException e) {
            logger.error("Email authentication failed when sending to {}", to, e);
            throw new EmailServiceException("Failed to authenticate with email server", e);
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new EmailServiceException("Failed to send email", e);
        }
    }

    private void validateEmail(String email) {
        try {
            new InternetAddress(email).validate();
        } catch (AddressException e) {
            logger.error("Invalid email address: {}", email);
            throw new EmailServiceException("Invalid email address", e);
        }
    }
}