package com.moviebooking.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.moviebooking.rest.dto.BookTicketRequest;
import com.moviebooking.rest.dto.TicketDto;
import com.moviebooking.service.EmailService;
import com.moviebooking.showtime.ShowTime;
import com.moviebooking.showtime.ShowTimeService;
import com.moviebooking.ticket.Ticket;
import com.moviebooking.ticket.TicketService;
import com.moviebooking.user.User;
import com.moviebooking.user.UserService;

import static com.moviebooking.config.SwaggerConfig.BEARER_KEY_SECURITY_SCHEME;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final UserService userService;
    private final ShowTimeService showTimeService;
    private final EmailService emailService;

    @Operation(security = {@SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME)})
    @GetMapping
    public List getUserTickets(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.validateAndGetUserByUsername(userDetails.getUsername());
        return ticketService.getTicketsByUser(user).stream()
                .map(TicketDto::from)
                .collect(Collectors.toList());
    }

    @Operation(security = {@SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME)})
    @GetMapping("/active")
    public List getActiveUserTickets(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.validateAndGetUserByUsername(userDetails.getUsername());
        return ticketService.getActiveTicketsByUser(user).stream()
                .map(TicketDto::from)
                .collect(Collectors.toList());
    }

    @Operation(security = {@SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME)})
    @GetMapping("/{id}")
    public TicketDto getTicket(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Ticket ticket = ticketService.getTicketById(id);
        User user = userService.validateAndGetUserByUsername(userDetails.getUsername());
        if (!ticket.getUser().getId().equals(user.getId()) && !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("Access denied");
        }
        return TicketDto.from(ticket);
    }

    @Operation(security = {@SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME)})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public TicketDto bookTicket(@Valid @RequestBody BookTicketRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.validateAndGetUserByUsername(userDetails.getUsername());
        ShowTime showTime = showTimeService.getShowTimeById(request.showTimeId());
        Ticket ticket = ticketService.bookTicket(user, showTime, request.numberOfSeats());
        
        try {
            emailService.sendBookingConfirmationEmail(ticket);
        } catch (Exception e) {
            System.err.println("Failed to send booking confirmation email: " + e.getMessage());
        }
        
        return TicketDto.from(ticket);
    }

    @Operation(security = {@SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME)})
    @PostMapping("/{id}/cancel")
    public TicketDto cancelTicket(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Ticket ticket = ticketService.getTicketById(id);
        User user = userService.validateAndGetUserByUsername(userDetails.getUsername());
        if (!ticket.getUser().getId().equals(user.getId()) && !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("Access denied");
        }
        
        Ticket cancelledTicket = ticketService.cancelTicket(id);
        
        try {
            emailService.sendBookingCancellationEmail(cancelledTicket);
        } catch (Exception e) {
            System.err.println("Failed to send booking cancellation email: " + e.getMessage());
        }
        
        return TicketDto.from(cancelledTicket);
    }

    @Operation(security = {@SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME)})
    @PostMapping("/{id}/cancel-seats")
    public TicketDto cancelSeats(
            @PathVariable Long id,
            @RequestParam int seatsToCancel,
            @AuthenticationPrincipal UserDetails userDetails) {

        Ticket ticket = ticketService.getTicketById(id);
        User user = userService.validateAndGetUserByUsername(userDetails.getUsername());

        // Ensure user can only modify their own tickets
        if (!ticket.getUser().getId().equals(user.getId()) && !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("Access denied");
        }

        Ticket updatedTicket = ticketService.cancelSeats(id, seatsToCancel);
        return TicketDto.from(updatedTicket);
    }

    @Operation(security = {@SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME)})
    @DeleteMapping("/{id}")
    public TicketDto deleteTicket(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Ticket ticket = ticketService.getTicketById(id);
        if (!userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("Access denied");
        }
        ticketService.deleteTicket(ticket);
        return TicketDto.from(ticket);
    }
}