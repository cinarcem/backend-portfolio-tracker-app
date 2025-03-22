package com.portfoliotracker.portfolioservice.controller;

import com.portfoliotracker.portfolioservice.common.ApiCustomResponse;
import com.portfoliotracker.portfolioservice.common.ErrorDetails;
import com.portfoliotracker.portfolioservice.dto.response.PortfolioStockResponse;
import com.portfoliotracker.portfolioservice.dto.response.PortfolioTransactionResponse;
import com.portfoliotracker.portfolioservice.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/portfolio/v1")
public class PortfolioStocksApi {

    private final PortfolioService portfolioService;
    private static final Logger logger = LogManager.getLogger(PortfolioTransactionApi.class);

    /*@GetMapping("/users/{userId}/stocks")
    @Operation(
            summary = "Returns list of user portfolio stocks with their market data.",
            description = "This endpoint returns list of user portfolio stocks with their market data."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode  = "200", description  = "Portfolio stocks received successfully."),
            @ApiResponse(responseCode  = "204", description  = "Stocks not found for userId.")
    })
    public ResponseEntity<ApiCustomResponse<List<PortfolioStockResponse>>> getUserPortfolioStocks(
            WebRequest webRequest,
            @PathVariable String userId
    ){
        String path = webRequest.getDescription(false).replace("uri=", "");
        List<ErrorDetails> errors = new ArrayList<>(List.of());
        List<PortfolioStockResponse> userPortfolioStocks = portfolioService.getUserPortfolioStocks(userId);

        ApiCustomResponse<List<PortfolioStockResponse>> apiCustomResponse = ApiCustomResponse
                .<List<PortfolioStockResponse>>builder()
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
    }*/

    @GetMapping("/users/{userId}/stocks")
    @Operation(
            summary = "Returns a page of user portfolio stocks list with their market data.",
            description = "This endpoint returns a page of user portfolio stocks list with their market data."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode  = "200", description  = "Portfolio stocks received successfully."),
            @ApiResponse(responseCode  = "204", description  = "Stocks not found for userId.")
    })
    public ResponseEntity<ApiCustomResponse<List<PortfolioStockResponse>>> getUserPortfolioStocks(
            WebRequest webRequest,
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "stockSymbol") String sortBy,
            @RequestParam(defaultValue = "false") boolean descending
    ){
        String path = webRequest.getDescription(false).replace("uri=", "");
        List<ErrorDetails> errors = new ArrayList<>(List.of());

        Sort sort = descending ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        List<PortfolioStockResponse> userPortfolioStocks = portfolioService.getUserPortfolioStocks(userId, pageable);

        ApiCustomResponse<List<PortfolioStockResponse>> apiCustomResponse = ApiCustomResponse
                .<List<PortfolioStockResponse>>builder()
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
