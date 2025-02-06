package com.portfoliotracker.marketdata.common;

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
