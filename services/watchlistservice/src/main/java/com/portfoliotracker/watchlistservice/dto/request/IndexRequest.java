package com.portfoliotracker.watchlistservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class IndexRequest {

    @NotBlank(message = "indexSymbol cannot be blank")
    private String indexSymbol;

}
