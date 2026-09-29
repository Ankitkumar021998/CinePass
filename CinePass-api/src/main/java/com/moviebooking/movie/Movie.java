package com.moviebooking.movie;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

import com.moviebooking.rest.dto.CreateMovieRequest;

@Data
@NoArgsConstructor
@Entity
@Table(name = "movies")
public class Movie {

    @Id
    private String imdb;

    private String title;
    private String poster;
    private String description;
    private Integer durationMinutes;
    private String genre;
    private String director;
    private Integer releaseYear;
    private Double rating;

    private Instant createdAt;

    public Movie(String imdb, String title, String poster) {
        this.imdb = imdb;
        this.title = title;
        this.poster = poster;
    }
    
    public Movie(String imdb, String title, String poster, String description, Integer durationMinutes, 
                String genre, String director, Integer releaseYear, Double rating) {
        this.imdb = imdb;
        this.title = title;
        this.poster = poster;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.genre = genre;
        this.director = director;
        this.releaseYear = releaseYear;
        this.rating = rating;
    }

    @PrePersist
    public void onPrePersist() {
        createdAt = Instant.now();
    }

    public static Movie from(CreateMovieRequest createMovieRequest) {
        return new Movie(
            createMovieRequest.imdb(), 
            createMovieRequest.title(), 
            createMovieRequest.poster(),
            createMovieRequest.description(),
            createMovieRequest.durationMinutes(),
            createMovieRequest.genre(),
            createMovieRequest.director(),
            createMovieRequest.releaseYear(),
            createMovieRequest.rating()
        );
    }
}
