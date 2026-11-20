package com.moviebooking.movie;

import java.util.List;

public interface MovieService {

    List<Movie> getMovies();

    List<Movie> getMoviesContainingText(String text);
    
    List<Movie> getMoviesByGenre(String genre);
    
    List<Movie> getMoviesByDirector(String director);
    
    List<Movie> getMoviesByReleaseYear(Integer releaseYear);
    
    List<Movie> getMoviesByMaxDuration(Integer maxDuration);

    Movie validateAndGetMovie(String imdb);

    Movie saveMovie(Movie movie);

    void deleteMovie(Movie movie);
}
