package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;
    private static final String FROM_EMAIL = "test@sender.com";
    private static final String BASE_URL = "http://test.com";

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "fromEmail", FROM_EMAIL);
        ReflectionTestUtils.setField(emailService, "baseUrl", BASE_URL);
    }

    @Test
    void whenSendVerificationEmail_thenEmailSent() {
        String toEmail = "test@example.com";
        String token = "test-token";
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendVerificationEmail(toEmail, token);

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(FROM_EMAIL, sentMessage.getFrom());
        String[] recipients = sentMessage.getTo();
        assertNotNull(recipients, "Recipients array should not be null");
        assertEquals(toEmail, recipients[0]);
        String messageText = sentMessage.getText();
        assertNotNull(messageText, "Message text should not be null");
        assertTrue(messageText.contains(BASE_URL + "/api/auth/verify?token=" + token));
    }

    @Test
    void whenSendResetPasswordEmail_thenEmailSent() {
        String toEmail = "test@example.com";
        String token = "reset-token";
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendResetPasswordEmail(toEmail, token);

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(FROM_EMAIL, sentMessage.getFrom());
        String[] recipients = sentMessage.getTo();
        assertNotNull(recipients, "Recipients array should not be null");
        assertEquals(toEmail, recipients[0]);
        String messageText = sentMessage.getText();
        assertNotNull(messageText, "Message text should not be null");
        assertTrue(messageText.contains(BASE_URL + "/api/auth/update-password?token=" + token));
    }

    @Test
    void whenInvalidEmail_thenThrowsEmailServiceException() {
        String invalidEmail = "invalid-email";
        
        assertThrows(EmailServiceException.class, () -> 
            emailService.sendVerificationEmail(invalidEmail, "token"));
    }

    @Test
    void whenMailAuthenticationFails_thenThrowsEmailServiceException() {
        String toEmail = "test@example.com";
        doThrow(new MailAuthenticationException("Auth failed"))
            .when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(EmailServiceException.class, () -> 
            emailService.sendVerificationEmail(toEmail, "token"));
    }
} 