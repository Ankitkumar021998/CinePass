package com.moviebooking.showtime;

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
import java.time.LocalDateTime;

import com.moviebooking.movie.Movie;

@Data
@NoArgsConstructor
@Entity
@Table(name = "showtimes")
public class ShowTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Movie movie;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String hall;
    private Double price;
    private Integer availableSeats;
    private Instant createdAt;

    public ShowTime(Movie movie, LocalDateTime startTime, LocalDateTime endTime, String hall, Double price, Integer availableSeats) {
        this.movie = movie;
        this.startTime = startTime;
        this.endTime = endTime;
        this.hall = hall;
        this.price = price;
        this.availableSeats = availableSeats;
    }

    @PrePersist
    public void onPrePersist() {
        createdAt = Instant.now();
    }
}