package com.moviebooking.rest.dto;

import java.time.Instant;

import com.moviebooking.ticket.Ticket;
import com.moviebooking.ticket.TicketStatus;

public record TicketDto(
        Long id,
        UserDto user,
        ShowTimeDto showTime,
        Integer numberOfSeats,
        Double totalPrice,
        String bookingReference,
        TicketStatus status,
        Instant createdAt) {

    public static TicketDto from(Ticket ticket) {
        return new TicketDto(
                ticket.getId(),
                UserDto.from(ticket.getUser()),
                ShowTimeDto.from(ticket.getShowTime()),
                ticket.getNumberOfSeats(),
                ticket.getTotalPrice(),
                ticket.getBookingReference(),
                ticket.getStatus(),
                ticket.getCreatedAt());
    }
}