package com.portfoliotracker.watchlistservice.common;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
public class ErrorDetails {
    private int status;
    private String message;
    private String path;
}
