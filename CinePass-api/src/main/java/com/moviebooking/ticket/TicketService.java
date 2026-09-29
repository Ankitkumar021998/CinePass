package com.moviebooking.ticket;

import java.util.List;

import com.moviebooking.showtime.ShowTime;
import com.moviebooking.user.User;

public interface TicketService {

    List<Ticket> getTicketsByUser(User user);
    
    List<Ticket> getActiveTicketsByUser(User user);
    
    Ticket getTicketById(Long id);
    
    Ticket bookTicket(User user, ShowTime showTime, Integer numberOfSeats);
    
    Ticket cancelTicket(Long ticketId);
    Ticket cancelSeats(Long ticketId, int seatsToCancel);
    
    void deleteTicket(Ticket ticket);
}