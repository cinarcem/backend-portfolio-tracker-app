package com.portfoliotracker.marketdata.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidSymbolsException extends  RuntimeException{

    public InvalidSymbolsException(String givenStockSymbols){

        super(String.format("Symbols are not valid. Given stock symbols are '%s'",givenStockSymbols));

    }
}
