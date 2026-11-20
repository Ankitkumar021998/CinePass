package com.moviebooking.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateMovieRequest(
        @Schema(example = "tt0117998") @NotBlank String imdb,
        @Schema(example = "Twister") @NotBlank String title,
        @Schema(example = "https://m.media-amazon.com/images/M/MV5BODExYTM0MzEtZGY2Yy00N2ExLTkwZjItNGYzYTRmMWZlOGEzXkEyXkFqcGdeQXVyNDk3NzU2MTQ@._V1_SX300.jpg") String poster,
        @Schema(example = "A disaster film about storm chasers pursuing tornadoes in Oklahoma.") String description,
        @Schema(example = "116") Integer durationMinutes,
        @Schema(example = "Action, Adventure, Thriller") String genre,
        @Schema(example = "Jan de Bont") String director,
        @Schema(example = "1996") Integer releaseYear,
        @Schema(example = "6.4") Double rating) {
}
