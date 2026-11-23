package com.tradexchange.controller;

import com.tradexchange.dto.response.ApiResponse;
import com.tradexchange.dto.response.MarketChartData;
import com.tradexchange.entity.MarketEntity;
import com.tradexchange.enums.MarketType;
import com.tradexchange.service.MarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/markets")
@Tag(name = "Markets", description = "Market catalog and chart data endpoints")
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @GetMapping
    @Operation(summary = "Get all markets with optional filters")
    public ResponseEntity<ApiResponse<List<MarketEntity>>> getMarkets(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) MarketType type,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "100") int limit) {

        List<MarketEntity> markets = marketService.getMarkets(active, type, search, limit);

        List<Map<String, Object>> frontendMarkets = markets.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("symbol", m.getSymbol());
            map.put("price", m.getLastPrice() != null ? m.getLastPrice() : 0.0);
            map.put("change", 0.0);
            map.put("displayName", m.getDisplayName());
            map.put("baseAsset", m.getBaseAsset());
            map.put("quoteAsset", m.getQuoteAsset());
            map.put("type", m.getType() != null ? m.getType().getValue() : null);
            map.put("volume24h", m.getVolume24h());
            map.put("status", m.getStatus() != null ? m.getStatus().getValue() : null);
            return map;
        }).toList();

        ApiResponse<List<MarketEntity>> res = new ApiResponse<>();
        res.setSuccess(true);
        res.setCount(markets.size());
        res.setData(markets);
        res.setMarkets(frontendMarkets);

        return ResponseEntity.ok(res);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get markets by asset type (crypto, stock, commodity)")
    public ResponseEntity<ApiResponse<List<MarketEntity>>> getMarketsByType(@PathVariable MarketType type) {
        List<MarketEntity> markets = marketService.getMarketsByType(type);
        return ResponseEntity.ok(ApiResponse.ok(markets, markets.size()));
    }

    @GetMapping("/chart")
    @Operation(summary = "Get historical / synthetic OHLC chart data for a symbol")
    public ResponseEntity<List<MarketChartData>> getChartData(@RequestParam String symbol) {
        List<MarketChartData> chartData = marketService.getChartData(symbol);
        return ResponseEntity.ok(chartData);
    }

    @GetMapping("/{symbol}")
    @Operation(summary = "Get single market details by symbol")
    public ResponseEntity<ApiResponse<MarketEntity>> getMarketBySymbol(@PathVariable String symbol) {
        MarketEntity market = marketService.getMarketBySymbol(symbol);
        return ResponseEntity.ok(ApiResponse.ok(market));
    }
}
