package com.portfoliotracker.watchlistservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NO_CONTENT)
public class ResourceNotFoundException extends  RuntimeException{

    public ResourceNotFoundException(String resourceName, String fieldName, String fieldValue){

        super(String.format("%s not found for %s : '%s'", resourceName, fieldName, fieldValue));

    }
}
