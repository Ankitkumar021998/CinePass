package com.moviebooking.showtime;

import java.time.LocalDateTime;
import java.util.List;

import com.moviebooking.movie.Movie;

public interface ShowTimeService {

    List<ShowTime> getAllShowTimes();
    
    List<ShowTime> getUpcomingShowTimes();
    
    List<ShowTime> getShowTimesByMovie(Movie movie);
    
    List<ShowTime> getUpcomingShowTimesByMovie(Movie movie);
    
    ShowTime getShowTimeById(Long id);
    
    ShowTime saveShowTime(ShowTime showTime);
    
    void deleteShowTime(ShowTime showTime);
    
    boolean updateAvailableSeats(Long showTimeId, int seatsToBook);
}