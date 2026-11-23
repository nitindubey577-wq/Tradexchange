package com.tradexchange.controller;

import com.tradexchange.dto.response.CandlesResponse;
import com.tradexchange.service.CandleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/candles")
@Tag(name = "Candles", description = "OHLC candlestick data endpoints")
public class CandleController {

    private final CandleService candleService;

    public CandleController(CandleService candleService) {
        this.candleService = candleService;
    }

    @GetMapping
    @Operation(summary = "Get OHLC candlestick data for a symbol and interval")
    public ResponseEntity<CandlesResponse> getCandles(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "1m") String interval,
            @RequestParam(defaultValue = "200") int limit) {

        CandlesResponse response = candleService.getCandles(symbol, interval, limit);
        return ResponseEntity.ok(response);
    }
}
