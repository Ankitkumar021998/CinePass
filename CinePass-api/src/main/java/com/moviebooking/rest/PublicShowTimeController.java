package com.moviebooking.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moviebooking.movie.Movie;
import com.moviebooking.movie.MovieService;
import com.moviebooking.rest.dto.ShowTimeDto;
import com.moviebooking.showtime.ShowTimeService;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/public/showtimes")
public class PublicShowTimeController {

    private final ShowTimeService showTimeService;
    private final MovieService movieService;

    @GetMapping
    public List<ShowTimeDto> getUpcomingShowTimes() {
        return showTimeService.getUpcomingShowTimes().stream()
                .map(ShowTimeDto::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/movie/{imdb}")
    public List<ShowTimeDto> getShowTimesByMovie(@PathVariable String imdb) {
        Movie movie = movieService.validateAndGetMovie(imdb);
        return showTimeService.getUpcomingShowTimesByMovie(movie).stream()
                .map(ShowTimeDto::from)
                .collect(Collectors.toList());
    }
}