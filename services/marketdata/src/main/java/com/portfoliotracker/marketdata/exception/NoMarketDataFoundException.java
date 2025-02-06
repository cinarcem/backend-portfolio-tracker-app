package com.portfoliotracker.marketdata.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class NoMarketDataFoundException extends  RuntimeException{

    public NoMarketDataFoundException(){
        super("No market data found. Check if stock symbols fetched properly.");
    }
}
