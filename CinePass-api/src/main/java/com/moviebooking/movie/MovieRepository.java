package com.moviebooking.movie;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, String> {

    List<Movie> findAllByOrderByTitle();

    List<Movie> findByImdbContainingOrTitleContainingIgnoreCaseOrderByTitle(String imdb, String title);
    
    List<Movie> findByGenreContainingIgnoreCaseOrderByTitle(String genre);
    
    List<Movie> findByDirectorContainingIgnoreCaseOrderByTitle(String director);
    
    List<Movie> findByReleaseYearOrderByTitle(Integer releaseYear);
    
    List<Movie> findByDurationMinutesLessThanEqualOrderByDurationMinutes(Integer maxDuration);
}
