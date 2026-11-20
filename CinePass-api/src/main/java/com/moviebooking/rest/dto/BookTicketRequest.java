package com.moviebooking.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BookTicketRequest(
        @Schema(example = "1") @NotNull Long showTimeId,
        @Schema(example = "2") @NotNull @Min(1) Integer numberOfSeats) {
}