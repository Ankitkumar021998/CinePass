package com.moviebooking.rest;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.moviebooking.movie.Movie;
import com.moviebooking.movie.MovieService;
import com.moviebooking.rest.dto.MovieDto;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/public/movies")
public class PublicMovieController {

    private final MovieService movieService;

    @Operation
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
    
    @Operation
    @GetMapping("/{imdb}")
    public MovieDto getMovie(@PathVariable String imdb) {
        Movie movie = movieService.validateAndGetMovie(imdb);
        return MovieDto.from(movie);
    }
}