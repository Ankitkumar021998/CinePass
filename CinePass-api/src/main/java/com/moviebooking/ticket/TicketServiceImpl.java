package com.moviebooking.ticket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moviebooking.service.EmailService;
import com.moviebooking.showtime.ShowTime;
import com.moviebooking.showtime.ShowTimeService;
import com.moviebooking.user.User;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final ShowTimeService showTimeService;
    private final EmailService emailService;

    @Override
    public List getTicketsByUser(User user) {
        return ticketRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public List getActiveTicketsByUser(User user) {
        return ticketRepository.findByUserAndStatus(user, TicketStatus.BOOKED);
    }

    @Override
    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(String.format("Ticket with id %d not found", id)));
    }

    @Override
    @Transactional
    public Ticket bookTicket(User user, ShowTime showTime, Integer numberOfSeats) {
        boolean seatsUpdated = showTimeService.updateAvailableSeats(showTime.getId(), numberOfSeats);
        if (!seatsUpdated) {
            throw new RuntimeException("Not enough seats available");
        }
        
        Double totalPrice = showTime.getPrice() * numberOfSeats;
        String bookingReference = generateBookingReference();
        
        Ticket ticket = new Ticket(user, showTime, numberOfSeats, totalPrice, bookingReference);
        Ticket savedTicket = ticketRepository.save(ticket);

        // Booking confirmation email trigger
        try {
            emailService.sendBookingConfirmationEmail(savedTicket);
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email for ticket ID {}: {}", savedTicket.getId(), e.getMessage());
        }

        return savedTicket;
    }

    @Override
    @Transactional
    public Ticket cancelTicket(Long ticketId) {
        Ticket ticket = getTicketById(ticketId);
        
        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new RuntimeException("Ticket is already cancelled");
        }
        
        // Return seats to available pool
        ShowTime showTime = ticket.getShowTime();
        showTime.setAvailableSeats(showTime.getAvailableSeats() + ticket.getNumberOfSeats());
        showTimeService.saveShowTime(showTime);
        
        ticket.setStatus(TicketStatus.CANCELLED);
        Ticket cancelledTicket = ticketRepository.save(ticket);

        // Cancellation email trigger
        try {
            emailService.sendBookingCancellationEmail(cancelledTicket);
        } catch (Exception e) {
            log.error("Failed to send cancellation email for ticket ID {}: {}", cancelledTicket.getId(), e.getMessage());
        }

        return cancelledTicket;
    }
    
    @Override
    @Transactional
    public Ticket cancelSeats(Long ticketId, int seatsToCancel) {
        Ticket ticket = getTicketById(ticketId);

        if (seatsToCancel <= 0 || seatsToCancel >= ticket.getNumberOfSeats()) {
            throw new IllegalArgumentException("Seats to cancel must be between 1 and " + (ticket.getNumberOfSeats() - 1));
        }

     // 1. Seats kam karein
        int remainingSeats = ticket.getNumberOfSeats() - seatsToCancel;
        ticket.setNumberOfSeats(remainingSeats);
        if (ticket.getShowTime() != null && ticket.getShowTime().getPrice() != null) {
            ticket.setTotalPrice(remainingSeats * ticket.getShowTime().getPrice());
        }

        // 2. ShowTime me seats wapas badhayein
        ShowTime showTime = ticket.getShowTime();
        if (showTime != null) {
            showTime.setAvailableSeats(showTime.getAvailableSeats() + seatsToCancel);
            showTimeService.saveShowTime(showTime);
        }

        Ticket updatedTicket = ticketRepository.save(ticket);

        // 3. Email notification trigger karein
        try {
            emailService.sendBookingCancellationEmail(updatedTicket);
        } catch (Exception e) {
            log.error("Failed to send partial cancellation email for ticket ID {}: {}", ticketId, e.getMessage());
        }

        return updatedTicket;
    }

    @Override
    public void deleteTicket(Ticket ticket) {
        ticketRepository.delete(ticket);
    }
    
    private String generateBookingReference() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}