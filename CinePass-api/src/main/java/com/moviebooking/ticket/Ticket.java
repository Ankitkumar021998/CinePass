package com.moviebooking.ticket;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

import com.moviebooking.showtime.ShowTime;
import com.moviebooking.user.User;

@Data
@NoArgsConstructor
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private ShowTime showTime;

    private Integer numberOfSeats;
    private Double totalPrice;
    private String bookingReference;
    private TicketStatus status;
    private Instant createdAt;

    public Ticket(User user, ShowTime showTime, Integer numberOfSeats, Double totalPrice, String bookingReference) {
        this.user = user;
        this.showTime = showTime;
        this.numberOfSeats = numberOfSeats;
        this.totalPrice = totalPrice;
        this.bookingReference = bookingReference;
        this.status = TicketStatus.BOOKED;
    }

    @PrePersist
    public void onPrePersist() {
        createdAt = Instant.now();
    }
}