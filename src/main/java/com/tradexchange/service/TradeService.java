package com.tradexchange.service;

import com.tradexchange.engine.events.TradeEvent;
import com.tradexchange.entity.TradeEntity;
import com.tradexchange.exception.TradingPlatformException;
import com.tradexchange.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class TradeService {

    private static final Logger log = LoggerFactory.getLogger(TradeService.class);

    private final TradeRepository tradeRepository;

    public TradeService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    @Transactional
    public TradeEntity saveTrade(TradeEvent event) {
        TradeEntity trade = new TradeEntity();
        trade.setTradeId(event.getId());
        trade.setBuyOrderId(event.getBuyOrderId());
        trade.setSellOrderId(event.getSellOrderId());
        trade.setSymbol(event.getSymbol().toUpperCase());
        trade.setPrice(event.getPrice());
        trade.setQuantity(event.getQuantity());
        trade.setValue(event.getPrice() * event.getQuantity());
        trade.setBuyFee(0.0);
        trade.setSellFee(0.0);
        trade.setTimestamp(Instant.ofEpochMilli(event.getTimestamp()));

        return tradeRepository.save(trade);
    }

    public List<TradeEntity> getAllTrades(String symbol, int limit) {
        int maxLimit = (limit > 0) ? Math.min(limit, 500) : 50;
        String sym = (symbol != null && !symbol.trim().isEmpty()) ? symbol.toUpperCase().trim() : null;
        return tradeRepository.findTradesWithFilter(sym, PageRequest.of(0, maxLimit));
    }

    public TradeEntity getTradeById(String tradeId) {
        if (tradeId == null || tradeId.trim().isEmpty()) {
            throw new TradingPlatformException("TRADE_ID_REQUIRED");
        }
        return tradeRepository.findByTradeId(tradeId)
                .orElseThrow(() -> new TradingPlatformException("TRADE_NOT_FOUND"));
    }

    public List<TradeEntity> getTradesByOrderId(String orderId, int limit) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new TradingPlatformException("ORDER_ID_REQUIRED");
        }
        int maxLimit = (limit > 0) ? Math.min(limit, 500) : 100;
        return tradeRepository.findByOrderId(orderId, PageRequest.of(0, maxLimit));
    }

    public List<TradeEntity> getTradesBySymbol(String symbol, int limit) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new TradingPlatformException("SYMBOL_REQUIRED");
        }
        return getAllTrades(symbol, limit);
    }
}
