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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
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
    public ResponseEntity<ApiCustomResponse<List<IndexResultResponse>>> addIndexesToWatchlist(
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

        logger.info(String.format("New index added to watchlist for user '%s'.", userId));

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

    @DeleteMapping("/indexes")
    @Operation(
            summary = "Deletes index symbols from watchlist for user.",
            description = "This endpoint deletes index symbols from watchlist for user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode  = "200", description  = "Index deleted from watchlist received successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class))),
            @ApiResponse(responseCode  = "207", description  = "Indexes deleted partially.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class))),
            @ApiResponse(responseCode  = "400", description  = "No indexes deleted from watchlist!",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class)))
    })
    public ResponseEntity<ApiCustomResponse<List<IndexResultResponse>>> deleteIndexesFromWatchlist(
            WebRequest webRequest,
            @Parameter(description = "Deletes a list of index symbols from watchlist.")
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

        List<IndexResultResponse> deletedSymbols = indexesWatchlistService.deleteIndexesFromWatchlist(userId, symbols);

        long failedCount = deletedSymbols.stream()
                .filter(item -> "failed".equals(item.getStatus()))
                .count();

        long successCount = deletedSymbols.stream()
                .filter(item -> "success".equals(item.getStatus()))
                .count();

        HttpStatus httpStatus;
        String message;

        if( failedCount == 0){
            httpStatus = HttpStatus.OK;
            message = "Indexes successfully deleted from watchlist.";
        } else if ( successCount>0){
            httpStatus = HttpStatus.MULTI_STATUS;
            message = "Indexes deleted partially.";
        } else {
            httpStatus = HttpStatus.BAD_REQUEST;
            message = "No indexes deleted!";
        }

        ApiCustomResponse<List<IndexResultResponse> > apiCustomResponse = ApiCustomResponse
                .<List<IndexResultResponse> >builder()
                .timestamp(Instant.now())
                .success(true)
                .status(httpStatus.value())
                .message(message)
                .data(deletedSymbols)
                .errors(errors)
                .path(path)
                .build();

        logger.info(String.format("Indexes deleted from watchlist for userId %s.", userId)
        );

        return ResponseEntity.status(httpStatus).body(apiCustomResponse);
    }

    @GetMapping("/indexes/sample")
    @SecurityRequirements
    @Operation(
            summary = "Returns a page of sample index watchlist for demostration.",
            description = "This endpoint returns a page of sample index watchlist for demostration."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode  = "200", description  = "Sample index watchlist received successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class)))
    })
    public ResponseEntity<ApiCustomResponse<Page<IndexWithMarketDataResponse>>> getSampleIndexesWatchlist(
            WebRequest webRequest,
            @Parameter(description = "Set page -1 to receive all indexes in sample stock watchlist.")
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "indexSymbol") String sortBy,
            @RequestParam(defaultValue = "true") boolean descending
    ){
        String path = webRequest.getDescription(false).replace("uri=", "");
        List<ErrorDetails> errors = new ArrayList<>(List.of());

        Sort sort = descending ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Page<IndexWithMarketDataResponse> indexWithMarketDataResponses = indexesWatchlistService
                .getSampleIndexWatchlist(page, size, sort);

        ApiCustomResponse<Page<IndexWithMarketDataResponse>> apiCustomResponse = ApiCustomResponse
                .<Page<IndexWithMarketDataResponse>>builder()
                .timestamp(Instant.now())
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Sample index watchlist received successfully.")
                .data(indexWithMarketDataResponses)
                .errors(errors)
                .path(path)
                .build();

        return ResponseEntity.ok(apiCustomResponse);
    }

}
