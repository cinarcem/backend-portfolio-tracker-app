package com.portfoliotracker.portfolioservice.controller;

import com.portfoliotracker.portfolioservice.common.ApiCustomResponse;
import com.portfoliotracker.portfolioservice.common.ErrorDetails;
import com.portfoliotracker.portfolioservice.dto.response.PortfolioStockResponse;
import com.portfoliotracker.portfolioservice.exception.UserNotFoundException;
import com.portfoliotracker.portfolioservice.service.PortfolioService;
import com.portfoliotracker.portfolioservice.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/portfolio/api/v1")
public class PortfolioStocksApi {

    private final PortfolioService portfolioService;
    private static final Logger logger = LogManager.getLogger(PortfolioTransactionApi.class);

    @GetMapping("/stocks")
    @Operation(
            summary = "Returns a page of user portfolio stocks list with their market data.",
            description = "This endpoint returns a page of user portfolio stocks list with their market data."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode  = "200", description  = "Portfolio stocks received successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class))),
            @ApiResponse(responseCode  = "204", description  = "Stocks not found for userId.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class)))
    })
    public ResponseEntity<ApiCustomResponse<Page<PortfolioStockResponse>>> getUserPortfolioStocks(
            WebRequest webRequest,
            @Parameter(description = "Set page -1 to receive all portfolio stocks.")
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "stockSymbol") String sortBy,
            @RequestParam(defaultValue = "false") boolean descending
    ){
        String path = webRequest.getDescription(false).replace("uri=", "");
        List<ErrorDetails> errors = new ArrayList<>(List.of());
        String userId;

        try {
            String token = webRequest.getHeader("Authorization");
            userId = JwtUtil.getJwtSub(token);
        }catch (Exception UserNotFoundException){
            throw new UserNotFoundException();
        }

        Sort sort = descending ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Page<PortfolioStockResponse> userPortfolioStocks = portfolioService.getUserPortfolioStocks(userId, page, size, sort);

        ApiCustomResponse<Page<PortfolioStockResponse>> apiCustomResponse = ApiCustomResponse
                .<Page<PortfolioStockResponse>>builder()
                .timestamp(Instant.now())
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Portfolio stocks received successfully.")
                .data(userPortfolioStocks)
                .errors(errors)
                .path(path)
                .build();

        logger.info(String.format("User portfolio stocks received successfully for user '%s'.", userId));

        return ResponseEntity.ok(apiCustomResponse);
    }
}
