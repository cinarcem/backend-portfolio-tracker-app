package com.portfoliotracker.portfolioservice.exception;

import com.portfoliotracker.portfolioservice.common.ApiCustomResponse;
import com.portfoliotracker.portfolioservice.common.ErrorDetails;
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

    @ExceptionHandler(InvalidSymbolsException.class)
    public ResponseEntity<ApiCustomResponse<String>> handleInvalidSymbolsException(
            InvalidSymbolsException exception, WebRequest webRequest){

        String path = webRequest.getDescription(false).replace("uri=", "");
        List<ErrorDetails> errors = new ArrayList<>(List.of());

        ErrorDetails errorDetails = ErrorDetails.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(exception.getMessage())
                .path(path)
                .build();

        errors.add(errorDetails);

        ApiCustomResponse<String> apiCustomResponse = ApiCustomResponse.<String>builder()
                .timestamp(Instant.now())
                .success(false)
                .status(HttpStatus.BAD_REQUEST.value())
                .message(exception.getMessage())
                .data(null)
                .errors(errors)
                .path(path)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiCustomResponse);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiCustomResponse<String>> handleInvalidSymbolsException(
            UserNotFoundException exception, WebRequest webRequest){

        String path = webRequest.getDescription(false).replace("uri=", "");
        List<ErrorDetails> errors = new ArrayList<>(List.of());

        ErrorDetails errorDetails = ErrorDetails.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(exception.getMessage())
                .path(path)
                .build();

        errors.add(errorDetails);

        ApiCustomResponse<String> apiCustomResponse = ApiCustomResponse.<String>builder()
                .timestamp(Instant.now())
                .success(false)
                .status(HttpStatus.NOT_FOUND.value())
                .message(exception.getMessage())
                .data(null)
                .errors(errors)
                .path(path)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiCustomResponse);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiCustomResponse<String>> handleNoContentFoundException(
            ResourceNotFoundException exception, WebRequest webRequest){

        String path = webRequest.getDescription(false).replace("uri=", "");
        List<ErrorDetails> errors = new ArrayList<>(List.of());

        ErrorDetails errorDetails = ErrorDetails.builder()
                .status(HttpStatus.NO_CONTENT.value())
                .message(exception.getMessage())
                .path(path)
                .build();

        errors.add(errorDetails);

        ApiCustomResponse<String> apiCustomResponse = ApiCustomResponse.<String>builder()
                .timestamp(Instant.now())
                .success(false)
                .status(HttpStatus.NO_CONTENT.value())
                .message(exception.getMessage())
                .data(null)
                .errors(errors)
                .path(path)
                .build();

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiCustomResponse);
    }

    @ExceptionHandler(ResourceNotDeletedException.class)
    public ResponseEntity<ApiCustomResponse<String>> handleResourceNotDeletedException(
            ResourceNotFoundException exception, WebRequest webRequest){

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

    @ExceptionHandler(UnknownSortPropertyException.class)
    public ResponseEntity<ApiCustomResponse<String>> handleUnknownSortProperyException(
            UnknownSortPropertyException exception, WebRequest webRequest){

        String path = webRequest.getDescription(false).replace("uri=", "");
        List<ErrorDetails> errors = new ArrayList<>(List.of());

        ErrorDetails errorDetails = ErrorDetails.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(exception.getMessage())
                .path(path)
                .build();

        errors.add(errorDetails);

        ApiCustomResponse<String> apiCustomResponse = ApiCustomResponse.<String>builder()
                .timestamp(Instant.now())
                .success(false)
                .status(HttpStatus.BAD_REQUEST.value())
                .message(exception.getMessage())
                .data(null)
                .errors(errors)
                .path(path)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiCustomResponse);
    }
}
