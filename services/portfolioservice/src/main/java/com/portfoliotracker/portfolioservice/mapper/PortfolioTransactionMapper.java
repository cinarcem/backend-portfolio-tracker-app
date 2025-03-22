package com.portfoliotracker.portfolioservice.mapper;

import com.portfoliotracker.portfolioservice.dto.request.PortfolioTransactionRequest;
import com.portfoliotracker.portfolioservice.dto.response.PortfolioTransactionResponse;
import com.portfoliotracker.portfolioservice.entity.PortfolioTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PortfolioTransactionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "date", source = "dto.date")
    @Mapping(target = "stockSymbol", source = "dto.stockSymbol")
    @Mapping(target = "quantity", source = "dto.quantity")
    @Mapping(target = "price", source = "dto.price")
    PortfolioTransaction toEntity(String userId, PortfolioTransactionRequest dto);

    PortfolioTransactionRequest toRequestDto(PortfolioTransaction entity);

    PortfolioTransactionResponse toResponseDto(PortfolioTransaction entity);

}
