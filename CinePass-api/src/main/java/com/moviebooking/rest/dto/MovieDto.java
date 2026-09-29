package com.moviebooking.rest.dto;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.moviebooking.movie.Movie;

public record MovieDto(String imdb, String title, String poster, String description, Integer durationMinutes, 
                       String genre, String director, Integer releaseYear, Double rating, String createdAt) {

    public static MovieDto from(Movie movie) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault());
        String formattedCreatedAt = movie.getCreatedAt() != null ? 
            formatter.format(movie.getCreatedAt()) : null;
        return new MovieDto(
                movie.getImdb(),
                movie.getTitle(),
                movie.getPoster(),
                movie.getDescription(), 
                movie.getDurationMinutes(), 
                movie.getGenre(), 
                movie.getDirector(), 
                movie.getReleaseYear(), 
                movie.getRating(),
                formattedCreatedAt
        );
    }
}
