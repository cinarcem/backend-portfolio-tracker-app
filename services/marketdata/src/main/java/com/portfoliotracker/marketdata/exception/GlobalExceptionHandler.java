package com.portfoliotracker.marketdata.exception;

import com.portfoliotracker.marketdata.common.ApiCustomResponse;
import com.portfoliotracker.marketdata.common.ErrorDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiCustomResponse<String>> handleAllOtherExceptions(Exception exception,
                                                                              WebRequest webRequest) {

        String path = webRequest.getDescription(false).replace("uri=", "");
        List<ErrorDetails> errors = new ArrayList<>(List.of());

        ErrorDetails errorDetails = ErrorDetails.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(exception.getMessage())
                .path(path)
                .build();

        errors.add(errorDetails);

        ApiCustomResponse<String> apiCustomResponse = ApiCustomResponse.<String>builder()
                .timestamp(Instant.now())
                .success(false)
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(exception.getMessage())
                .data(null)
                .errors(errors)
                .path(path)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiCustomResponse);

    }

    @ExceptionHandler(NoMarketDataFoundException.class)
    public ResponseEntity<ErrorDetails> handleNoMarketDataFoundException(
            NoMarketDataFoundException exception, WebRequest webRequest){

        String path = webRequest.getDescription(false).replace("uri=", "");
        List<ErrorDetails> errors = new ArrayList<>(List.of());

        ErrorDetails errorDetails = ErrorDetails.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(exception.getMessage())
                .path(path)
                .build();

        errors.add(errorDetails);

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidSymbolsException.class)
    public ResponseEntity<ErrorDetails> handleInvalidStocksSymbolsException(
            InvalidSymbolsException exception, WebRequest webRequest){

        String path = webRequest.getDescription(false).replace("uri=", "");
        List<ErrorDetails> errors = new ArrayList<>(List.of());

        ErrorDetails errorDetails = ErrorDetails.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(exception.getMessage())
                .path(path)
                .build();

        errors.add(errorDetails);

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

}
