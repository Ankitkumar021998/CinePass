package com.moviebooking.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.moviebooking.ticket.Ticket;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    private String fromEmail = "noreply@cinepass.com";

    @Value("${app.name:CinePass}")
    private String appName;

    public void sendBookingConfirmationEmail(Ticket ticket) {
        log.info("Starting to send booking confirmation email to: {}", ticket.getUser().getEmail());
        
        try {
            log.debug("Creating MIME message...");
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            log.debug("Setting email properties - From: {}, To: {}", fromEmail, ticket.getUser().getEmail());
            helper.setFrom(fromEmail, appName);
            helper.setTo(ticket.getUser().getEmail());
            helper.setSubject("🎬 Booking Confirmed - " + ticket.getShowTime().getMovie().getTitle());

            log.debug("Preparing template context...");
            Context context = new Context(Locale.getDefault());
            context.setVariable("userName", ticket.getUser().getName());
            context.setVariable("movieTitle", ticket.getShowTime().getMovie().getTitle());
            context.setVariable("showDateTime", formatShowDateTime(ticket));
            context.setVariable("theaterName", ticket.getShowTime().getHall());
            context.setVariable("numberOfSeats", ticket.getNumberOfSeats());
            context.setVariable("ticketPrice", formatPrice(ticket.getShowTime().getPrice()));
            context.setVariable("totalAmount", formatPrice(ticket.getTotalPrice()));
            context.setVariable("bookingReference", ticket.getBookingReference());
            context.setVariable("poster", ticket.getShowTime().getMovie().getPoster());

            log.debug("Processing email template...");
            String htmlContent = templateEngine.process("booking-confirmation", context);
            helper.setText(htmlContent, true);

            log.debug("Sending email via mail sender...");
            mailSender.send(message);
            log.info("✅ Booking confirmation email sent successfully to: {}", ticket.getUser().getEmail());

        } catch (MessagingException e) {
            log.error("❌ MessagingException while sending booking confirmation email to: {}", ticket.getUser().getEmail(), e);
            throw new RuntimeException("Failed to send booking confirmation email: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Unexpected error while sending booking confirmation email to: {}", ticket.getUser().getEmail(), e);
            throw new RuntimeException("Unexpected error while sending email: " + e.getMessage(), e);
        }
    }

    public void sendBookingCancellationEmail(Ticket ticket) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, appName);
            helper.setTo(ticket.getUser().getEmail());
            helper.setSubject("🎫 Booking Cancelled - " + ticket.getShowTime().getMovie().getTitle());

            String textContent = String.format(
                "Dear %s,\n\n" +
                "Your booking has been cancelled successfully.\n\n" +
                "Booking Details:\n" +
                "Movie: %s\n" +
                "Show Time: %s\n" +
                "Booking Reference: %s\n" +
                "Amount: $%.2f\n\n" +
                "If this was a paid booking, the refund will be processed within 3-5 business days.\n\n" +
                "Thank you for using %s!\n\n" +
                "Best regards,\n" +
                "The %s Team",
                ticket.getUser().getName(),
                ticket.getShowTime().getMovie().getTitle(),
                formatShowDateTime(ticket),
                ticket.getBookingReference(),
                ticket.getTotalPrice(),
                appName,
                appName
            );

            helper.setText(textContent, false);
            mailSender.send(message);
            log.info("Booking cancellation email sent successfully to: {}", ticket.getUser().getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send booking cancellation email to: {}", ticket.getUser().getEmail(), e);
            throw new RuntimeException("Failed to send booking cancellation email", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending booking cancellation email to: {}", ticket.getUser().getEmail(), e);
            throw new RuntimeException("Unexpected error while sending email", e);
        }
    }

    private String formatShowDateTime(Ticket ticket) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' hh:mm a");
        return ticket.getShowTime().getStartTime().format(formatter);
    }

    private String formatPrice(Double price) {
        return String.format("%.2f", price);
    }
}