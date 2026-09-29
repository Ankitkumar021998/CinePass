package com.moviebooking.rest.dto;

import java.time.LocalDateTime;

import com.moviebooking.showtime.ShowTime;

public record ShowTimeDto(
        Long id,
        MovieDto movie,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String hall,
        Double price,
        Integer availableSeats) {

    public static ShowTimeDto from(ShowTime showTime) {
        return new ShowTimeDto(
                showTime.getId(),
                showTime.getMovie() != null ? MovieDto.from(showTime.getMovie()) : null,
                showTime.getStartTime(),
                showTime.getEndTime(),
                showTime.getHall(),
                showTime.getPrice(),
                showTime.getAvailableSeats());
    }
}