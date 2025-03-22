package com.portfoliotracker.portfolioservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidSymbolsException extends  RuntimeException{

    public InvalidSymbolsException(String givenStockSymbol){

        super(String.format("'%s' is not valid stock symbol.",givenStockSymbol));

    }
}
