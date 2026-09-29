package com.moviebooking.movie;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    @Override
    public List<Movie> getMovies() {
        return movieRepository.findAllByOrderByTitle();
    }

    @Override
    public List<Movie> getMoviesContainingText(String text) {
        return movieRepository.findByImdbContainingOrTitleContainingIgnoreCaseOrderByTitle(text, text);
    }
    
    @Override
    public List<Movie> getMoviesByGenre(String genre) {
        return movieRepository.findByGenreContainingIgnoreCaseOrderByTitle(genre);
    }
    
    @Override
    public List<Movie> getMoviesByDirector(String director) {
        return movieRepository.findByDirectorContainingIgnoreCaseOrderByTitle(director);
    }
    
    @Override
    public List<Movie> getMoviesByReleaseYear(Integer releaseYear) {
        return movieRepository.findByReleaseYearOrderByTitle(releaseYear);
    }
    
    @Override
    public List<Movie> getMoviesByMaxDuration(Integer maxDuration) {
        return movieRepository.findByDurationMinutesLessThanEqualOrderByDurationMinutes(maxDuration);
    }

    @Override
    public Movie validateAndGetMovie(String imdb) {
        return movieRepository.findById(imdb)
                .orElseThrow(() -> new MovieNotFoundException(String.format("Movie with imdb %s not found", imdb)));
    }

    @Override
    public Movie saveMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    @Override
    public void deleteMovie(Movie movie) {
        movieRepository.delete(movie);
    }
}
