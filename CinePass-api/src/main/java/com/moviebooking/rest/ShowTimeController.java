package com.moviebooking.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.moviebooking.movie.Movie;
import com.moviebooking.movie.MovieService;
import com.moviebooking.rest.dto.CreateShowTimeRequest;
import com.moviebooking.rest.dto.ShowTimeDto;
import com.moviebooking.showtime.ShowTime;
import com.moviebooking.showtime.ShowTimeService;

import static com.moviebooking.config.SwaggerConfig.BEARER_KEY_SECURITY_SCHEME;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/showtimes")
public class ShowTimeController {

    private final ShowTimeService showTimeService;
    private final MovieService movieService;

    @Operation(security = {@SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME)})
    @GetMapping
    public List<ShowTimeDto> getUpcomingShowTimes() {
        return showTimeService.getUpcomingShowTimes().stream()
                .map(ShowTimeDto::from)
                .collect(Collectors.toList());
    }

    @Operation(security = {@SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME)})
    @GetMapping("/all")
    public List<ShowTimeDto> getAllShowTimes() {
        return showTimeService.getAllShowTimes().stream()
                .map(ShowTimeDto::from)
                .collect(Collectors.toList());
    }

    @Operation(security = {@SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME)})
    @GetMapping("/movie/{imdb}")
    public List<ShowTimeDto> getShowTimesByMovie(@PathVariable String imdb) {
        Movie movie = movieService.validateAndGetMovie(imdb);
        return showTimeService.getUpcomingShowTimesByMovie(movie).stream()
                .map(ShowTimeDto::from)
                .collect(Collectors.toList());
    }

    @Operation(security = {@SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME)})
    @GetMapping("/{id}")
    public ShowTimeDto getShowTime(@PathVariable Long id) {
        return ShowTimeDto.from(showTimeService.getShowTimeById(id));
    }

    @Operation(security = {@SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME)})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ShowTimeDto createShowTime(@Valid @RequestBody CreateShowTimeRequest request) {
        Movie movie = movieService.validateAndGetMovie(request.movieImdb());
        ShowTime showTime = new ShowTime(
                movie,
                request.startTime(),
                request.endTime(),
                request.hall(),
                request.price(),
                request.availableSeats()
        );
        return ShowTimeDto.from(showTimeService.saveShowTime(showTime));
    }

    @Operation(security = {@SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME)})
    @DeleteMapping("/{id}")
    public ShowTimeDto deleteShowTime(@PathVariable Long id) {
        ShowTime showTime = showTimeService.getShowTimeById(id);
        showTimeService.deleteShowTime(showTime);
        return ShowTimeDto.from(showTime);
    }
}