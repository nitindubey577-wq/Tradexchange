package com.tradexchange.service;

import com.tradexchange.dto.response.CandleData;
import com.tradexchange.dto.response.CandlesResponse;
import com.tradexchange.engine.events.TradeEvent;
import com.tradexchange.entity.TradeEntity;
import com.tradexchange.exception.TradingPlatformException;
import com.tradexchange.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CandleService {

    private static final Logger log = LoggerFactory.getLogger(CandleService.class);

    private final TradeRepository tradeRepository;
    private final Map<String, CandleData> candleStore = new ConcurrentHashMap<>();

    private static final Map<String, Long> INTERVAL_MAP = Map.of(
            "1m", 60L * 1000L,
            "5m", 5L * 60L * 1000L,
            "15m", 15L * 60L * 1000L,
            "1h", 60L * 60L * 1000L
    );

    public CandleService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public CandleData updateRealtimeCandle(TradeEvent trade, String interval) {
        Long bucketSize = INTERVAL_MAP.get(interval != null ? interval : "1m");
        if (bucketSize == null) return null;

        long time = trade.getTimestamp();
        long bucket = (time / bucketSize) * bucketSize;
        String key = trade.getSymbol().toUpperCase() + "_" + interval + "_" + bucket;

        CandleData candle = candleStore.compute(key, (k, existing) -> {
            if (existing == null) {
                return new CandleData(
                        Instant.ofEpochMilli(bucket),
                        trade.getPrice(),
                        trade.getPrice(),
                        trade.getPrice(),
                        trade.getPrice(),
                        trade.getQuantity()
                );
            } else {
                existing.setHigh(Math.max(existing.getHigh(), trade.getPrice()));
                existing.setLow(Math.min(existing.getLow(), trade.getPrice()));
                existing.setClose(trade.getPrice());
                existing.setVolume(existing.getVolume() + trade.getQuantity());
                return existing;
            }
        });

        // Memory control (keep under 1000 items)
        if (candleStore.size() > 1000) {
            Iterator<String> it = candleStore.keySet().iterator();
            if (it.hasNext()) {
                candleStore.remove(it.next());
            }
        }

        return candle;
    }

    public CandlesResponse getCandles(String symbol, String interval, int limit) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new TradingPlatformException("SYMBOL_REQUIRED");
        }

        String intvl = (interval != null && !interval.trim().isEmpty()) ? interval.trim() : "1m";
        Long bucketSize = INTERVAL_MAP.get(intvl);
        if (bucketSize == null) {
            throw new TradingPlatformException("INVALID_INTERVAL");
        }

        int maxLimit = (limit > 0) ? Math.min(limit, 500) : 200;

        List<TradeEntity> trades = tradeRepository.findBySymbolOrderByTimestampAsc(symbol.toUpperCase().trim());
        Map<Long, CandleData> buckets = new LinkedHashMap<>();

        for (TradeEntity trade : trades) {
            long time = trade.getTimestamp().toEpochMilli();
            long bucket = (time / bucketSize) * bucketSize;

            buckets.compute(bucket, (b, existing) -> {
                if (existing == null) {
                    return new CandleData(
                            Instant.ofEpochMilli(bucket),
                            trade.getPrice(),
                            trade.getPrice(),
                            trade.getPrice(),
                            trade.getPrice(),
                            trade.getQuantity()
                    );
                } else {
                    existing.setHigh(Math.max(existing.getHigh(), trade.getPrice()));
                    existing.setLow(Math.min(existing.getLow(), trade.getPrice()));
                    existing.setClose(trade.getPrice());
                    existing.setVolume(existing.getVolume() + trade.getQuantity());
                    return existing;
                }
            });
        }

        List<Long> allBuckets = new ArrayList<>(buckets.keySet());
        allBuckets.sort(Collections.reverseOrder());

        List<Long> latestBuckets = allBuckets.subList(0, Math.min(allBuckets.size(), maxLimit));
        Collections.sort(latestBuckets);

        List<CandleData> candles = new ArrayList<>();
        for (Long b : latestBuckets) {
            candles.add(buckets.get(b));
        }

        return new CandlesResponse(true, symbol.toUpperCase().trim(), intvl, candles);
    }
}
