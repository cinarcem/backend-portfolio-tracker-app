package com.portfoliotracker.portfolioservice.controller;

import com.portfoliotracker.portfolioservice.common.ApiCustomResponse;
import com.portfoliotracker.portfolioservice.common.ErrorDetails;
import com.portfoliotracker.portfolioservice.dto.request.PortfolioTransactionRequest;
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
public class PortfolioTransactionApi {

    private final PortfolioService portfolioService;
    private static final Logger logger = LogManager.getLogger(PortfolioTransactionApi.class);

    @PostMapping("/users/{userId}/transactions")
    @Operation(
            summary = "Add portfolio transaction for a user.",
            description = "This endpoint adds a portfolio transaction for a user"
    )
    public ResponseEntity<ApiCustomResponse<PortfolioTransactionResponse>> addPortfolioTransaction
            (
                    WebRequest webRequest,
                    @PathVariable String userId,
                    @RequestBody PortfolioTransactionRequest portfolioTransactionRequest
            )
    {

        String path = webRequest.getDescription(false).replace("uri=", "");
        List<ErrorDetails> errors = new ArrayList<>(List.of());

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

    @GetMapping("/users/{userId}/transactions")
    @Operation(
            summary = "Returns a page of transactions list of added stocks by user id.",
            description = "This endpoint returns a page of transactions list of added stocks by user id."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode  = "200", description  = "User transactions received successfully."),
            @ApiResponse(responseCode  = "204", description  = "No user transaction found.")
    })
    public  ResponseEntity<ApiCustomResponse<List<PortfolioTransactionResponse>>>getPageableUserTransactions(
            WebRequest webRequest,
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean descending
    ){

        String path = webRequest.getDescription(false).replace("uri=", "");
        List<ErrorDetails> errors = new ArrayList<>(List.of());

        Sort sort = descending ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        List<PortfolioTransactionResponse> userTransactions = portfolioService.getPortfolioTransactionsByUserId(userId, pageable);

        ApiCustomResponse<List<PortfolioTransactionResponse>> apiCustomResponse = ApiCustomResponse
                .<List<PortfolioTransactionResponse>>builder()
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

    @GetMapping("/users/{userId}/all-transactions")
    @Operation(
            summary = "Returns all transactions list of added stocks by user id.",
            description = "This endpoint returns transactions list of added stocks by user id."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode  = "200", description  = "User transactions received successfully."),
            @ApiResponse(responseCode  = "204", description  = "No user transaction found.")
    })
    public ResponseEntity<ApiCustomResponse<List<PortfolioTransactionResponse>>> getUserTransactions(
            WebRequest webRequest,
            @PathVariable String userId
    ){
        String path = webRequest.getDescription(false).replace("uri=", "");
        List<ErrorDetails> errors = new ArrayList<>(List.of());

        List<PortfolioTransactionResponse> userTransactions = portfolioService
                .getAllPortfolioTransactionsByUserId(userId);

        ApiCustomResponse<List<PortfolioTransactionResponse>> apiCustomResponse = ApiCustomResponse
                .<List<PortfolioTransactionResponse>>builder()
                .timestamp(Instant.now())
                .success(true)
                .status(HttpStatus.OK.value())
                .message("User transactions received successfully.")
                .data(userTransactions)
                .errors(errors)
                .path(path)
                .build();
        logger.info(String.format("User transactions received successfully for user '%s'.", userId));

        return ResponseEntity.ok(apiCustomResponse);
    }

    @DeleteMapping("/users/{userId}/transactions/{transactionId}")
    @Operation(
            summary = "Deletes a transaction by user id and transaction id.",
            description = "This endpoint deletes a transaction by user id and transaction id."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode  = "204 ", description  = "Transaction with transaction id for user id is deleted successfully."),
            @ApiResponse(responseCode  = "404 ", description  = "Transaction not found for userId/transactionId."),
            @ApiResponse(responseCode  = "500", description  = "Transaction not deleted with userId/transactionId.'")
    })
    public ResponseEntity<ApiCustomResponse<String>> deleteUserTransaction(
            WebRequest webRequest,
            @PathVariable String userId,
            @PathVariable Long transactionId
    ){
        String path = webRequest.getDescription(false).replace("uri=", "");
        List<ErrorDetails> errors = new ArrayList<>(List.of());

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

    @DeleteMapping("/users/{userId}/stocks/{stockSymbol}/transactions")
    @Operation(
            summary = "Deletes all transactions for a stock symbol by user id.",
            description = "This endpoint deletes ll transactions for a stock symbol by user id."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode  = "204 ", description  = "Transaction with stock symbol for userId is deleted successfully."),
            @ApiResponse(responseCode  = "404 ", description  = "Transaction not found for userId/transactionId."),
            @ApiResponse(responseCode  = "500", description  = "Transaction not deleted with userId/transactionId.'")
    })
    public ResponseEntity<ApiCustomResponse<String>> deleteAllTransactionsByUserIdAndStockSymbol(
            WebRequest webRequest,
            @PathVariable String userId,
            @PathVariable String stockSymbol
    ){
        String path = webRequest.getDescription(false).replace("uri=", "");
        List<ErrorDetails> errors = new ArrayList<>(List.of());

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
