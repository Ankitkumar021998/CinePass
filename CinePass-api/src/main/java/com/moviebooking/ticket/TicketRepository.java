package com.moviebooking.ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.moviebooking.showtime.ShowTime;
import com.moviebooking.user.User;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByUser(User user);
    
    List<Ticket> findByUserOrderByCreatedAtDesc(User user);
    
    List<Ticket> findByShowTime(ShowTime showTime);
    
    List<Ticket> findByUserAndStatus(User user, TicketStatus status);
}