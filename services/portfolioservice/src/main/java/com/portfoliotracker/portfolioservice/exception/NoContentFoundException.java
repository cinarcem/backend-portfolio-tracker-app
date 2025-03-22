package com.portfoliotracker.portfolioservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NO_CONTENT)
public class NoContentFoundException extends RuntimeException{
    public NoContentFoundException(String resourceName, String fieldName, String fieldValue){
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
    }
}
