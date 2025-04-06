package com.portfoliotracker.watchlistservice.common;

import lombok.*;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class ApiCustomResponse<T> {

    private Instant timestamp;
    private boolean success;
    private int status;
    private String message;
    private T data;
    private List<ErrorDetails> errors;
    private String path;

}
