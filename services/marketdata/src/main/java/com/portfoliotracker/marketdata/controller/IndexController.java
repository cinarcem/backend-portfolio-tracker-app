package com.portfoliotracker.marketdata.controller;

import com.portfoliotracker.marketdata.common.ApiCustomResponse;
import com.portfoliotracker.marketdata.common.ErrorDetails;
import com.portfoliotracker.marketdata.model.Index;
import com.portfoliotracker.marketdata.model.Stock;
import com.portfoliotracker.marketdata.service.IndexService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequiredArgsConstructor
@RequestMapping("/market-data/v1")
public class IndexController {

    private final IndexService indexService;

    @GetMapping("/all-index-symbols")
    @ApiResponses(value = {
            @ApiResponse(responseCode  = "200", description  = "Index symbols successfully received."),
            @ApiResponse(responseCode  = "500", description  = "No market data found.")
    })
    public ResponseEntity<ApiCustomResponse< Map<String, String>>> getAllIndexSymbols(WebRequest webRequest){

        String path = webRequest.getDescription(false).replace("uri=", "");
        List<ErrorDetails> errors = new ArrayList<>(List.of());

        Map<String, String> allIndexSymbols  = indexService.getAllIndexSymbolsAndNames();

        ApiCustomResponse< Map<String, String>> apiCustomResponse = ApiCustomResponse.< Map<String, String>>builder()
                .timestamp(Instant.now())
                .success(true)
                .status(HttpStatus.OK.value())
                .message(String.format("%d index symbols successfully received.",allIndexSymbols.size()))
                .data(allIndexSymbols)
                .errors(errors)
                .path(path)
                .build();

        return ResponseEntity.ok(apiCustomResponse);

    }

    @GetMapping("/indexes-market-data")
    @ApiResponses(value = {
            @ApiResponse(responseCode  = "200", description  = "Index symbols successfully received."),
            @ApiResponse(responseCode  = "206", description  = "Partial data received."),
            @ApiResponse(responseCode  = "500", description  = "No market data found.")
    })
    public ResponseEntity<ApiCustomResponse<List<Index>>> getIndexesMarketData(WebRequest webRequest, @RequestParam List<String> symbols) {
        String path = webRequest.getDescription(false).replace("uri=", "");
        String responseMessage;
        List<ErrorDetails> errors = new ArrayList<>(List.of());

        Map<String, Index> serviceResponse = indexService.getIndexesMarketData(symbols);
        List<Index> indexesMarketData = new ArrayList<>(serviceResponse.values());

        int differenceCount = symbols.size() - indexesMarketData.size();
        boolean isAllSymbolsReceived = differenceCount == 0;



        if(isAllSymbolsReceived) {
            responseMessage = String.format("%d index symbols successfully received.",indexesMarketData.size());
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

        ApiCustomResponse<List<Index>> apiCustomResponse = ApiCustomResponse.<List<Index>>builder()
                .timestamp(Instant.now())
                .success(true)
                .status( isAllSymbolsReceived ? HttpStatus.OK.value() : HttpStatus.PARTIAL_CONTENT.value())
                .message(responseMessage)
                .data(indexesMarketData)
                .errors(errors)
                .path(path)
                .build();

        return ResponseEntity.ok(apiCustomResponse);
    }
}
