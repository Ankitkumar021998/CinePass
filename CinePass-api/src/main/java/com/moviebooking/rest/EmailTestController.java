package com.moviebooking.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.moviebooking.service.EmailService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/test")
public class EmailTestController {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @GetMapping("/email")
    public String testEmail(@RequestParam String to) {
        try {
            log.info("Testing email configuration...");
            log.info("From email: {}", fromEmail);
            log.info("To email: {}", to);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("CinePass Email Test");
            message.setText("This is a test email from CinePass application. If you receive this, email configuration is working correctly!");

            log.info("Sending test email...");
            mailSender.send(message);
            log.info("✅ Test email sent successfully!");

            return "✅ Test email sent successfully to: " + to;

        } catch (Exception e) {
            log.error("❌ Failed to send test email", e);
            return "❌ Failed to send test email: " + e.getMessage();
        }
    }

    @GetMapping("/config")
    public String checkEmailConfig() {
        return String.format(
            "Email Configuration:\n" +
            "From Email: %s\n" +
            "Mail Sender: %s\n" +
            "Status: %s",
            fromEmail,
            mailSender.getClass().getSimpleName(),
            mailSender != null ? "✅ Available" : "❌ Not Available"
        );
    }
}
