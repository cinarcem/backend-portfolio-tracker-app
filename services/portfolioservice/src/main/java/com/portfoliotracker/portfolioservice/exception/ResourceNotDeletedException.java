package com.portfoliotracker.portfolioservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class ResourceNotDeletedException extends RuntimeException{

    public ResourceNotDeletedException(String resourceName, String fieldName, String fieldValue){
        super(String.format("%s not deleted with %s : '%s'", resourceName, fieldName, fieldValue));
    }
}
