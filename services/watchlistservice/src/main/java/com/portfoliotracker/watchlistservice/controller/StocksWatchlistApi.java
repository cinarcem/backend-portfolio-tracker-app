package com.portfoliotracker.watchlistservice.controller;

import com.portfoliotracker.watchlistservice.common.ApiCustomResponse;
import com.portfoliotracker.watchlistservice.common.ErrorDetails;
import com.portfoliotracker.watchlistservice.dto.response.StockResultResponse;
import com.portfoliotracker.watchlistservice.dto.response.StockWithMarketDataResponse;
import com.portfoliotracker.watchlistservice.exception.UserNotFoundException;
import com.portfoliotracker.watchlistservice.service.StocksWatchlistService;
import com.portfoliotracker.watchlistservice.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotEmpty;
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

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/watchlist/api/v1")
public class StocksWatchlistApi {

    private static final Logger logger = LogManager.getLogger(StocksWatchlistApi.class);
    private final StocksWatchlistService stocksWatchlistService;

    @PostMapping("/stocks")
    @Operation(
            summary = "Adds stock symbols to watchlist for user.",
            description = "This endpoint adds stock symbols to watchlist for user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode  = "200", description  = "Stock added to watchlist received successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class))),
            @ApiResponse(responseCode  = "207", description  = "Stocks added partially. Check if the given symbols are valid.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class))),
            @ApiResponse(responseCode  = "400", description  = "No stocks added!",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class)))
    })
    public ResponseEntity<ApiCustomResponse<List<StockResultResponse>>> addUserStocksWatchlist(
            WebRequest webRequest,
            @Parameter(description = "A list of stock symbols to add watchlist.")
            @RequestParam @NotEmpty(message = "Symbols list cannot be empty") List<String> symbols
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

        List<StockResultResponse> addedSymbols = stocksWatchlistService.addStockToWatchlist(userId, symbols);

        long failedCount = addedSymbols.stream()
                .filter(item -> "failed".equals(item.getStatus()))
                .count();

        long successCount = addedSymbols.stream()
                .filter(item -> "success".equals(item.getStatus()))
                .count();

        HttpStatus httpStatus;
        String message;

        if( failedCount == 0){
            httpStatus = HttpStatus.OK;
            message = "Stocks added to watchlist successfully.";
        } else if ( successCount>0){
            httpStatus = HttpStatus.MULTI_STATUS;
            message = "Stocks added partially. Check if the given symbols are valid.";
        } else {
            httpStatus = HttpStatus.BAD_REQUEST;
            message = "No stocks added!";
        }

        ApiCustomResponse<List<StockResultResponse> > apiCustomResponse = ApiCustomResponse
                .<List<StockResultResponse> >builder()
                .timestamp(Instant.now())
                .success(true)
                .status(httpStatus.value())
                .message(message)
                .data(addedSymbols)
                .errors(errors)
                .path(path)
                .build();

        logger.info(String.format("New stocks added to wathclist for user '%s'.", userId));

        return ResponseEntity.status(httpStatus).body(apiCustomResponse);

    }

    @GetMapping("/stocks")
    @Operation(
            summary = "Returns a page of stocks followed by user.",
            description = "This endpoint returns a page of stocks followed by user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode  = "200", description  = "User stocks watchlist received successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class))),
            @ApiResponse(responseCode  = "204", description  = "No added stocks found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class)))
    })
    public ResponseEntity<ApiCustomResponse<Page<StockWithMarketDataResponse>>> getUserStocksWatchlist(
            WebRequest webRequest,
            @Parameter(description = "Set page -1 to receive all stocks in watchlist.")
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "stockSymbol") String sortBy,
            @RequestParam(defaultValue = "true") boolean descending
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
        Page<StockWithMarketDataResponse> stockWithMarketDataResponses = stocksWatchlistService.
                getStockWatchlists(userId, page, size, sort);

        ApiCustomResponse<Page<StockWithMarketDataResponse> > apiCustomResponse = ApiCustomResponse
                .<Page<StockWithMarketDataResponse> >builder()
                .timestamp(Instant.now())
                .success(true)
                .status(HttpStatus.OK.value())
                .message("User stocks watchlist received successfully.")
                .data(stockWithMarketDataResponses)
                .errors(errors)
                .path(path)
                .build();

        logger.info(String.format("User stocks watchlist received successfully for user '%s'.", userId));

        return ResponseEntity.ok(apiCustomResponse);

    }

}
