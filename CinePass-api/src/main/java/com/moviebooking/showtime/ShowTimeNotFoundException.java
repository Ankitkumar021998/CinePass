package com.moviebooking.showtime;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ShowTimeNotFoundException extends RuntimeException {

    public ShowTimeNotFoundException(String message) {
        super(message);
    }
}