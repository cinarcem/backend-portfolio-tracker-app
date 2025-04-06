package com.portfoliotracker.portfolioservice.controller;

import com.portfoliotracker.portfolioservice.common.ApiCustomResponse;
import com.portfoliotracker.portfolioservice.common.ErrorDetails;
import com.portfoliotracker.portfolioservice.dto.request.PortfolioTransactionRequest;
import com.portfoliotracker.portfolioservice.dto.response.PortfolioTransactionResponse;
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

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/portfolio/api/v1")
public class PortfolioTransactionApi {

    private final PortfolioService portfolioService;
    private static final Logger logger = LogManager.getLogger(PortfolioTransactionApi.class);

    @PostMapping("/transaction")
    @Operation(
            summary = "Add portfolio transaction for a user.",
            description = "This endpoint adds a portfolio transaction for a user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode  = "200", description  = "User transactions received successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class)))
    })
    public ResponseEntity<ApiCustomResponse<PortfolioTransactionResponse>> addPortfolioTransaction
            (
                    WebRequest webRequest,
                    @RequestBody PortfolioTransactionRequest portfolioTransactionRequest
            )
    {

        String path = webRequest.getDescription(false).replace("uri=", "");
        List<ErrorDetails> errors = new ArrayList<>(List.of());
        String userId;

        try {
            String token = webRequest.getHeader("Authorization");
            userId = JwtUtil.getJwtSub(token);
        }catch (Exception UserNotFoundException){
            throw new UserNotFoundException();
        }

        PortfolioTransactionResponse savedPortfolioTransaction = portfolioService
                .savePortfolioTransaction(userId, portfolioTransactionRequest);

        ApiCustomResponse<PortfolioTransactionResponse> apiCustomResponse = ApiCustomResponse
                .<PortfolioTransactionResponse>builder()
                .timestamp(Instant.now())
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Portfolio transaction added successfully.")
                .data(savedPortfolioTransaction)
                .errors(errors)
                .path(path)
                .build();
        logger.info(
                String.format("'%s' portfolio transaction added successfully for user '%s'."
                ,portfolioTransactionRequest.getStockSymbol() , userId)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(apiCustomResponse);
    }

    @GetMapping("/transactions")
    @Operation(
            summary = "Returns a page of transactions list of added stocks by user id.",
            description = "This endpoint returns a page of transactions list of added stocks by user id."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode  = "200", description  = "User transactions received successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class))),
            @ApiResponse(responseCode  = "204", description  = "No user transaction found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class)))
    })
    public  ResponseEntity<ApiCustomResponse<Page<PortfolioTransactionResponse>>>getUserTransactions(
            WebRequest webRequest,
            @Parameter(description = "Set page -1 to receive all transactions.")
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
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
        Page<PortfolioTransactionResponse> userTransactions = portfolioService.getPortfolioTransactionsByUserId(userId, page, size, sort);

        ApiCustomResponse<Page<PortfolioTransactionResponse>> apiCustomResponse = ApiCustomResponse
                .<Page<PortfolioTransactionResponse>>builder()
                .timestamp(Instant.now())
                .success(true)
                .status(HttpStatus.OK.value())
                .message("User transactions received successfully.")
                .data(userTransactions)
                .errors(errors)
                .path(path)
                .build();
        return ResponseEntity.ok(apiCustomResponse);
    }

    @DeleteMapping("/transactions/{transactionId}")
    @Operation(
            summary = "Deletes a transaction by user id and transaction id.",
            description = "This endpoint deletes a transaction by user id and transaction id."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode  = "204 ", description  = "Transaction with transaction id for user id is deleted successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class))),
            @ApiResponse(responseCode  = "404 ", description  = "Transaction not found for userId/transactionId.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class))),
            @ApiResponse(responseCode  = "500", description  = "Transaction not deleted with userId/transactionId.'",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class)))
    })
    public ResponseEntity<ApiCustomResponse<String>> deleteUserTransaction(
            WebRequest webRequest,
            @PathVariable Long transactionId
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

        portfolioService.deletePortfolioTransaction(userId, transactionId);

        ApiCustomResponse<String> apiCustomResponse = ApiCustomResponse
                .<String>builder()
                .timestamp(Instant.now())
                .success(true)
                .status(HttpStatus.NO_CONTENT.value())
                .message(
                        String.format(
                        "Transaction with '%s' transaction id for userId '%s' is deleted successfully.",
                        transactionId, userId)
                )
                .data(null)
                .errors(errors)
                .path(path)
                .build();

        logger.info(
                    String.format("Transaction with '%s' transaction id for userId '%s' is deleted successfully.",
                    transactionId, userId)
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiCustomResponse);

    }

    @DeleteMapping("/stocks/{stockSymbol}/transactions")
    @Operation(
            summary = "Deletes all transactions for a stock symbol by user id.",
            description = "This endpoint deletes ll transactions for a stock symbol by user id."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode  = "204 ", description  = "Transaction with stock symbol for userId is deleted successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class))),
            @ApiResponse(responseCode  = "404 ", description  = "Transaction not found for userId/transactionId.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class))),
            @ApiResponse(responseCode  = "500", description  = "Transaction not deleted with userId/transactionId.'",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class)))
    })
    public ResponseEntity<ApiCustomResponse<String>> deleteAllTransactionsByStockSymbol(
            WebRequest webRequest,
            @PathVariable String stockSymbol
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

        portfolioService.deleteAllTransactionsByUserIdAndStockSymbol(userId,stockSymbol);

        ApiCustomResponse<String> apiCustomResponse = ApiCustomResponse
                .<String>builder()
                .timestamp(Instant.now())
                .success(true)
                .status(HttpStatus.NO_CONTENT.value())
                .message(
                        String.format("Transaction with '%s' stock symbol for userId '%s' is deleted successfully.",
                        stockSymbol, userId)
                )
                .data(null)
                .errors(errors)
                .path(path)
                .build();

        logger.info(
                String.format(
                        "Transaction with '%s' stock symbol for userId '%s' is deleted successfully.",
                        stockSymbol, userId)
        );

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiCustomResponse);

    }
}
