package com.tradexchange.service;

import com.tradexchange.dto.response.MarketChartData;
import com.tradexchange.entity.MarketEntity;
import com.tradexchange.enums.MarketType;
import com.tradexchange.exception.TradingPlatformException;
import com.tradexchange.repository.MarketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MarketService {

    private static final Logger log = LoggerFactory.getLogger(MarketService.class);

    private final MarketRepository marketRepository;

    public MarketService(MarketRepository marketRepository) {
        this.marketRepository = marketRepository;
    }

    public List<MarketEntity> getMarkets(Boolean active, MarketType type, String search, int limit) {
        Boolean isActive = (active != null) ? active : true;
        int pageLimit = (limit > 0) ? Math.min(limit, 500) : 100;
        return marketRepository.findMarketsWithFilter(isActive, type, search, PageRequest.of(0, pageLimit));
    }

    public MarketEntity getMarketBySymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new TradingPlatformException("SYMBOL_REQUIRED");
        }
        return marketRepository.findBySymbol(symbol.toUpperCase().trim())
                .orElseThrow(() -> new TradingPlatformException("MARKET_NOT_FOUND"));
    }

    public List<MarketEntity> getMarketsByType(MarketType type) {
        if (type == null) {
            throw new TradingPlatformException("TYPE_REQUIRED");
        }
        return marketRepository.findByTypeAndIsActive(type, true);
    }

    public List<MarketChartData> getChartData(String symbol) {
        MarketEntity market = getMarketBySymbol(symbol);
        double base = (market.getLastPrice() != null && market.getLastPrice() > 0) ? market.getLastPrice() : 100.0;
        long now = System.currentTimeMillis();

        List<MarketChartData> result = new ArrayList<>();
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        for (int i = 0; i < 50; i++) {
            double open = base + rand.nextDouble(0, 10);
            double close = open + (rand.nextDouble() - 0.5) * 10;
            double high = Math.max(open, close) + rand.nextDouble(0, 5);
            double low = Math.min(open, close) - rand.nextDouble(0, 5);
            double volume = rand.nextDouble(0, 100);
            long timeSeconds = (now - (50L - i) * 60_000L) / 1000L;

            result.add(new MarketChartData(timeSeconds, open, high, low, close, volume));
        }

        return result;
    }

    @Transactional
    public void updateMarketPriceAndVolume(String symbol, double price, double volume) {
        Optional<MarketEntity> opt = marketRepository.findBySymbol(symbol.toUpperCase().trim());
        if (opt.isPresent()) {
            MarketEntity m = opt.get();
            m.setLastPrice(price);
            m.setVolume24h((m.getVolume24h() != null ? m.getVolume24h() : 0.0) + volume);
            marketRepository.save(m);
        }
    }
}
