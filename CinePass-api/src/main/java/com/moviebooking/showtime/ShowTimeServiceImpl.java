package com.moviebooking.showtime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moviebooking.movie.Movie;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ShowTimeServiceImpl implements ShowTimeService {

    private final ShowTimeRepository showTimeRepository;

    @Override
    public List<ShowTime> getAllShowTimes() {
        return showTimeRepository.findAll();
    }

    @Override
    public List<ShowTime> getUpcomingShowTimes() {
        return showTimeRepository.findByStartTimeAfterOrderByStartTime(LocalDateTime.now());
    }

    @Override
    public List<ShowTime> getShowTimesByMovie(Movie movie) {
        return showTimeRepository.findByMovie(movie);
    }

    @Override
    public List<ShowTime> getUpcomingShowTimesByMovie(Movie movie) {
        return showTimeRepository.findByMovieAndStartTimeAfter(movie, LocalDateTime.now());
    }

    @Override
    public ShowTime getShowTimeById(Long id) {
        return showTimeRepository.findById(id)
                .orElseThrow(() -> new ShowTimeNotFoundException(String.format("ShowTime with id %d not found", id)));
    }

    @Override
    public ShowTime saveShowTime(ShowTime showTime) {
        return showTimeRepository.save(showTime);
    }

    @Override
    public void deleteShowTime(ShowTime showTime) {
        showTimeRepository.delete(showTime);
    }

    @Override
    @Transactional
    public boolean updateAvailableSeats(Long showTimeId, int seatsToBook) {
        ShowTime showTime = getShowTimeById(showTimeId);
        if (showTime.getAvailableSeats() >= seatsToBook) {
            showTime.setAvailableSeats(showTime.getAvailableSeats() - seatsToBook);
            showTimeRepository.save(showTime);
            return true;
        }
        return false;
    }
}