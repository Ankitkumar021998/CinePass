package com.moviebooking.showtime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.moviebooking.movie.Movie;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowTimeRepository extends JpaRepository<ShowTime, Long> {

    List<ShowTime> findByMovie(Movie movie);
    
    List<ShowTime> findByMovieAndStartTimeAfter(Movie movie, LocalDateTime dateTime);
    
    List<ShowTime> findByStartTimeAfterOrderByStartTime(LocalDateTime dateTime);
}