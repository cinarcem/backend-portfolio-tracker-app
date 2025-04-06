package com.portfoliotracker.watchlistservice.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class StockResultResponse {

    @NotBlank(message = "stockSymbol cannot be blank")
    private String stockSymbol;
    @NotBlank(message = "status cannot be blank")
    private String status;
    private String error;

}
