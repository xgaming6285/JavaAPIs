package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token) {
        String subject = "Email Verification";
        String confirmationUrl = "http://localhost:8080/api/auth/verify?token=" + token;
        String message = "Please verify your email by clicking the link: " + confirmationUrl;

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(to);
        email.setSubject(subject);
        email.setText(message);
        
        try {
            mailSender.send(email);
            logger.info("Verification email sent to {}", to);
        } catch (MailException e) {
            logger.error("Failed to send verification email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send verification email");
        }
    }

    public void sendResetPasswordEmail(String to, String token) {
        String subject = "Password Reset Request";
        String resetUrl = "http://localhost:8080/api/auth/update-password?token=" + token;
        String message = "Please reset your password by clicking the link: " + resetUrl;

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(to);
        email.setSubject(subject);
        email.setText(message);
        
        try {
            mailSender.send(email);
            logger.info("Reset password email sent to {}", to);
        } catch (MailException e) {
            logger.error("Failed to send reset password email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send reset password email");
        }
    }
} 