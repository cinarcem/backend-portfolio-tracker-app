package com.portfoliotracker.watchlistservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class UnknownSortPropertyException extends RuntimeException{

    public UnknownSortPropertyException(String sortingPropertyName){
        super(String.format("Unknown sort property: %s", sortingPropertyName));
    }
}
