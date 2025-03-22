package com.portfoliotracker.portfolioservice.mapper;

import com.portfoliotracker.portfolioservice.dto.request.PortfolioTransactionRequest;
import com.portfoliotracker.portfolioservice.dto.response.PortfolioTransactionResponse;
import com.portfoliotracker.portfolioservice.entity.PortfolioTransaction;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.annotations.CreationTimestamp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
