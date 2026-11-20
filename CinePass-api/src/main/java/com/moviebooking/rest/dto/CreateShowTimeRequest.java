package com.moviebooking.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateShowTimeRequest(
        @Schema(example = "tt0117998") @NotBlank String movieImdb,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,
        @Schema(example = "Hall A") @NotBlank String hall,
        @Schema(example = "12.99") @NotNull @Min(0) Double price,
        @Schema(example = "100") @NotNull @Min(1) Integer availableSeats) {
}