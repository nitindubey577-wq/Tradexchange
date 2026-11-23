package com.tradexchange.controller;

import com.tradexchange.dto.response.ApiResponse;
import com.tradexchange.entity.TradeEntity;
import com.tradexchange.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Trades", description = "Trade query endpoints")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @GetMapping("/trades")
    @Operation(summary = "Get all trades with optional symbol filter")
    public ResponseEntity<ApiResponse<List<TradeEntity>>> getAllTrades(
            @RequestParam(required = false) String symbol,
            @RequestParam(defaultValue = "50") int limit) {

        List<TradeEntity> trades = tradeService.getAllTrades(symbol, limit);
        return ResponseEntity.ok(ApiResponse.ok(trades, trades.size()));
    }

    @GetMapping("/trades/{id}")
    @Operation(summary = "Get single trade by ID")
    public ResponseEntity<ApiResponse<TradeEntity>> getTradeById(@PathVariable String id) {
        TradeEntity trade = tradeService.getTradeById(id);
        return ResponseEntity.ok(ApiResponse.ok(trade));
    }

    @GetMapping("/trades/order/{orderId}")
    @Operation(summary = "Get trades by order ID")
    public ResponseEntity<ApiResponse<List<TradeEntity>>> getTradesByOrderId(
            @PathVariable String orderId,
            @RequestParam(defaultValue = "100") int limit) {

        List<TradeEntity> trades = tradeService.getTradesByOrderId(orderId, limit);
        return ResponseEntity.ok(ApiResponse.ok(trades, trades.size()));
    }

    @GetMapping("/trades/symbol/{symbol}")
    @Operation(summary = "Get trades by market symbol")
    public ResponseEntity<ApiResponse<List<TradeEntity>>> getTradesBySymbol(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "50") int limit) {

        List<TradeEntity> trades = tradeService.getTradesBySymbol(symbol, limit);
        return ResponseEntity.ok(ApiResponse.ok(trades, trades.size()));
    }
}
