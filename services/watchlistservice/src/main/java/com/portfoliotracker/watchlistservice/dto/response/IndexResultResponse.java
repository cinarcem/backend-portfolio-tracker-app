package com.portfoliotracker.watchlistservice.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class IndexResultResponse {

    @NotBlank(message = "indexSymbol cannot be blank")
    private String indexSymbol;
    @NotBlank(message = "status cannot be blank")
    private String status;
    private String error;

}
