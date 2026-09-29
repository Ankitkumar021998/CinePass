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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.moviebooking.movie.Movie;
import com.moviebooking.movie.MovieService;
import com.moviebooking.rest.dto.CreateMovieRequest;
import com.moviebooking.rest.dto.MovieDto;

import static com.moviebooking.config.SwaggerConfig.BEARER_KEY_SECURITY_SCHEME;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    @Operation(security = {@SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME)})
    @GetMapping
    public List<MovieDto> getMovies(@RequestParam(value = "text", required = false) String text,
                                    @RequestParam(value = "genre", required = false) String genre,
                                    @RequestParam(value = "director", required = false) String director,
                                    @RequestParam(value = "releaseYear", required = false) Integer releaseYear,
                                    @RequestParam(value = "maxDuration", required = false) Integer maxDuration) {
        List<Movie> movies;
        if (text != null) {
            movies = movieService.getMoviesContainingText(text);
        } else if (genre != null) {
            movies = movieService.getMoviesByGenre(genre);
        } else if (director != null) {
            movies = movieService.getMoviesByDirector(director);
        } else if (releaseYear != null) {
            movies = movieService.getMoviesByReleaseYear(releaseYear);
        } else if (maxDuration != null) {
            movies = movieService.getMoviesByMaxDuration(maxDuration);
        } else {
            movies = movieService.getMovies();
        }
        return movies.stream()
                .map(MovieDto::from)
                .collect(Collectors.toList());
    }
    
    @Operation(security = {@SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME)})
    @GetMapping("/{imdb}")
    public MovieDto getMovie(@PathVariable String imdb) {
        Movie movie = movieService.validateAndGetMovie(imdb);
        return MovieDto.from(movie);
    }

    @Operation(security = {@SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME)})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public MovieDto createMovie(@Valid @RequestBody CreateMovieRequest createMovieRequest) {
        Movie movie = Movie.from(createMovieRequest);
        return MovieDto.from(movieService.saveMovie(movie));
    }

    @Operation(security = {@SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME)})
    @PutMapping("/{imdb}")
    public MovieDto updateMovie(@PathVariable String imdb, @Valid @RequestBody CreateMovieRequest updateMovieRequest) {
        Movie existingMovie = movieService.validateAndGetMovie(imdb);
        
        // Update the existing movie with new data
        existingMovie.setTitle(updateMovieRequest.title());
        existingMovie.setPoster(updateMovieRequest.poster());
        existingMovie.setDescription(updateMovieRequest.description());
        existingMovie.setDurationMinutes(updateMovieRequest.durationMinutes());
        existingMovie.setGenre(updateMovieRequest.genre());
        existingMovie.setDirector(updateMovieRequest.director());
        existingMovie.setReleaseYear(updateMovieRequest.releaseYear());
        existingMovie.setRating(updateMovieRequest.rating());
        
        return MovieDto.from(movieService.saveMovie(existingMovie));
    }

    @Operation(security = {@SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME)})
    @DeleteMapping("/{imdb}")
    public MovieDto deleteMovie(@PathVariable String imdb) {
        Movie movie = movieService.validateAndGetMovie(imdb);
        movieService.deleteMovie(movie);
        return MovieDto.from(movie);
    }
}
