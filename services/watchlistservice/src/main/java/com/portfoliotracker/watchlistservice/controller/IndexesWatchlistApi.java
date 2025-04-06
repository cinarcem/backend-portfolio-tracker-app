package com.portfoliotracker.watchlistservice.controller;

import com.portfoliotracker.watchlistservice.common.ApiCustomResponse;
import com.portfoliotracker.watchlistservice.common.ErrorDetails;
import com.portfoliotracker.watchlistservice.dto.response.IndexResultResponse;
import com.portfoliotracker.watchlistservice.dto.response.IndexWithMarketDataResponse;
import com.portfoliotracker.watchlistservice.exception.UserNotFoundException;
import com.portfoliotracker.watchlistservice.service.IndexesWatchlistService;
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
public class IndexesWatchlistApi {

    private static final Logger logger = LogManager.getLogger(IndexesWatchlistApi.class);
    private final IndexesWatchlistService indexesWatchlistService;

    @PostMapping("/indexes")
    @Operation(
            summary = "Adds index symbols to watchlist for user.",
            description = "This endpoint adds index symbols to watchlist for user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode  = "200", description  = "Index added to watchlist received successfully.",
                    content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ApiCustomResponse.class))),
            @ApiResponse(responseCode  = "207", description  = "Indexes added partially. Check if the given symbols are valid.",
                    content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ApiCustomResponse.class))),
            @ApiResponse(responseCode  = "400", description  = "No indexes added!",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class)))
    })
    public ResponseEntity<ApiCustomResponse<List<IndexResultResponse>>> addUserIndexesWatchlist(
            WebRequest webRequest,
            @Parameter(description = "A list of index symbols to add watchlist.")
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

        List<IndexResultResponse> addedSymbols = indexesWatchlistService.addIndexToWatchlist(userId, symbols);

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
            message = "Indexes added to watchlist successfully.";
        } else if ( successCount>0){
            httpStatus = HttpStatus.MULTI_STATUS;
            message = "Indexes added partially. Check if the given symbols are valid.";
        } else {
            httpStatus = HttpStatus.BAD_REQUEST;
            message = "No indexes added!";
        }

        ApiCustomResponse<List<IndexResultResponse> > apiCustomResponse = ApiCustomResponse
                .<List<IndexResultResponse> >builder()
                .timestamp(Instant.now())
                .success(true)
                .status(httpStatus.value())
                .message(message)
                .data(addedSymbols)
                .errors(errors)
                .path(path)
                .build();

        logger.info(String.format("New index added to wathclist for user '%s'.", userId));

        return ResponseEntity.status(httpStatus).body(apiCustomResponse);

    }

    @GetMapping("/indexes")
    @Operation(
            summary = "Returns a page of indexes followed by user.",
            description = "This endpoint returns a page of indexes followed by user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode  = "200", description  = "User indexes watchlist received successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class))),
            @ApiResponse(responseCode  = "204", description  = "No added indexes found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class)))
    })
    public ResponseEntity<ApiCustomResponse<Page<IndexWithMarketDataResponse> >> getUserIndexesWatchlist(
            WebRequest webRequest,
            @Parameter(description = "Set page -1 to receive all indexes in watchlist.")
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "indexSymbol") String sortBy,
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
        Page<IndexWithMarketDataResponse> indexWithMarketDataResponses = indexesWatchlistService
                .getWatchlistIndexes(userId, page, size, sort);

        ApiCustomResponse<Page<IndexWithMarketDataResponse> > apiCustomResponse = ApiCustomResponse
                .<Page<IndexWithMarketDataResponse> >builder()
                .timestamp(Instant.now())
                .success(true)
                .status(HttpStatus.OK.value())
                .message("User indexes watchlist received successfully.")
                .data(indexWithMarketDataResponses)
                .errors(errors)
                .path(path)
                .build();

        logger.info(String.format("User indexes watchlist received successfully for user '%s'.", userId));

        return ResponseEntity.ok(apiCustomResponse);

    }

}
