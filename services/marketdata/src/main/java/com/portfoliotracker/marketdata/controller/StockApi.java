package com.portfoliotracker.marketdata.controller;

import com.portfoliotracker.marketdata.common.ApiCustomResponse;
import com.portfoliotracker.marketdata.common.ErrorDetails;
import com.portfoliotracker.marketdata.dto.StockResponse;
import com.portfoliotracker.marketdata.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/market-data/api/v1")
public class StockApi {

    private final StockService stockService;

    @GetMapping("/stocks/symbols")
    @Operation(
            summary = "Get all stock symbols",
            description = "This endpoint retrieves all available stock symbols and their names from the market data. " +
                    "The response includes a map where the key is the stock symbol, " +
                    "and the value is the corresponding name."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode  = "200", description  = "Market data successfully received for given symbols.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class))),
            @ApiResponse(responseCode  = "500", description  = "No market data found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class)))
    })
    public ResponseEntity<ApiCustomResponse<List<String>>> getAllStockSymbols(WebRequest webRequest){

        String path = webRequest.getDescription(false).replace("uri=", "");
        List<ErrorDetails> errors = new ArrayList<>(List.of());
        List<String>allStockSymbols = stockService.getAllStockSymbols();

        ApiCustomResponse<List<String>> apiCustomResponse = ApiCustomResponse.<List<String>>builder()
                .timestamp(Instant.now())
                .success(true)
                .status(HttpStatus.OK.value())
                .message(String.format("%d stock symbols successfully received.",allStockSymbols.size()))
                .data(allStockSymbols)
                .errors(errors)
                .path(path)
                .build();

        return ResponseEntity.ok(apiCustomResponse);
    }

    @GetMapping("/stocks")
    @Operation(
            summary = "Retrieve market data for specified stocks.",
            description = "This endpoint retrieves market data for the given list of stock symbols. The response " +
                    "includes detailed market data. If some symbols are " +
                    "missing from the response, an appropriate message will be returned with the list of missing " +
                    "symbols."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode  = "200", description  = "Market data successfully received for given symbols.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class))),
            @ApiResponse(responseCode  = "206", description  = "Partial data received.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class))),
            @ApiResponse(responseCode  = "400", description  = "Symbols are not valid.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiCustomResponse.class)))
    })
    public ResponseEntity<ApiCustomResponse<List<StockResponse>>> getStocksMarketData(
            WebRequest webRequest,
            @Parameter(description = "A list of stock symbols to fetch market data.")
            @RequestParam @NotEmpty(message = "Symbols list cannot be empty") List<String> symbols) {

        String path = webRequest.getDescription(false).replace("uri=", "");
        String responseMessage;
        List<ErrorDetails> errors = new ArrayList<>(List.of());
        Map<String, StockResponse> serviceResponse = stockService.getStocksMarketData(symbols);
        List<StockResponse> stocksMarketData = new ArrayList<>(serviceResponse.values());

        int differenceCount = symbols.size() - stocksMarketData.size();
        boolean isAllSymbolsReceived = differenceCount == 0;

        if(isAllSymbolsReceived) {
            responseMessage = "Market data successfully received for given symbols.";
        } else{
            List<String> missingSymbols = symbols.stream()
                    .filter(key -> !serviceResponse.containsKey(key))
                    .toList();
            String missingSymbolsText = String.join(",", missingSymbols);
            responseMessage = String.format("Partial data received. Missing symbols are '%s'", missingSymbolsText);
            ErrorDetails errorDetails = ErrorDetails.builder()
                    .status(HttpStatus.PARTIAL_CONTENT.value())
                    .message(String.format("Partial data received. Missing symbols are '%s'", missingSymbolsText))
                    .path(path)
                    .build();
            errors.add(errorDetails);
        }

        ApiCustomResponse<List<StockResponse>> apiCustomResponse = ApiCustomResponse.<List<StockResponse>>builder()
                .timestamp(Instant.now())
                .success(true)
                .status(isAllSymbolsReceived ? HttpStatus.OK.value() : HttpStatus.PARTIAL_CONTENT.value())
                .message(responseMessage)
                .data(stocksMarketData)
                .errors(errors)
                .path(path)
                .build();

        if (isAllSymbolsReceived){
            return ResponseEntity.ok(apiCustomResponse);
        } else {
            return  ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(apiCustomResponse);
        }
    }
}
