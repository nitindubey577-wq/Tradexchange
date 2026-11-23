package com.tradexchange.service;

import com.tradexchange.entity.MarketEntity;
import com.tradexchange.enums.ContractType;
import com.tradexchange.enums.MarketStatus;
import com.tradexchange.enums.MarketType;
import com.tradexchange.repository.MarketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MarketSeedService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MarketSeedService.class);

    private final MarketRepository marketRepository;

    public MarketSeedService(MarketRepository marketRepository) {
        this.marketRepository = marketRepository;
    }

    private record SeedItem(String name, double price, MarketType category) {}

    private static final List<SeedItem> SEED_ITEMS = List.of(
            // CRYPTO (INR PAIRS)
            new SeedItem("BTC-INR", 6500000.0, MarketType.CRYPTO),
            new SeedItem("ETH-INR", 320000.0, MarketType.CRYPTO),
            new SeedItem("SOL-INR", 12000.0, MarketType.CRYPTO),
            new SeedItem("XRP-INR", 50.0, MarketType.CRYPTO),
            new SeedItem("ADA-INR", 40.0, MarketType.CRYPTO),
            new SeedItem("DOGE-INR", 10.0, MarketType.CRYPTO),
            new SeedItem("DOT-INR", 600.0, MarketType.CRYPTO),
            new SeedItem("MATIC-INR", 80.0, MarketType.CRYPTO),
            new SeedItem("AVAX-INR", 3000.0, MarketType.CRYPTO),
            new SeedItem("LTC-INR", 7000.0, MarketType.CRYPTO),
            new SeedItem("BNB-INR", 45000.0, MarketType.CRYPTO),
            new SeedItem("TRX-INR", 10.0, MarketType.CRYPTO),
            new SeedItem("SHIB-INR", 0.002, MarketType.CRYPTO),
            new SeedItem("ATOM-INR", 900.0, MarketType.CRYPTO),
            new SeedItem("LINK-INR", 1500.0, MarketType.CRYPTO),
            new SeedItem("UNI-INR", 700.0, MarketType.CRYPTO),
            new SeedItem("APT-INR", 900.0, MarketType.CRYPTO),
            new SeedItem("ARB-INR", 120.0, MarketType.CRYPTO),
            new SeedItem("OP-INR", 250.0, MarketType.CRYPTO),
            new SeedItem("FIL-INR", 500.0, MarketType.CRYPTO),

            // US STOCKS
            new SeedItem("AAPL", 180.0, MarketType.STOCK),
            new SeedItem("TSLA", 240.0, MarketType.STOCK),
            new SeedItem("AMZN", 175.0, MarketType.STOCK),
            new SeedItem("GOOGL", 150.0, MarketType.STOCK),
            new SeedItem("MSFT", 410.0, MarketType.STOCK),
            new SeedItem("NVDA", 900.0, MarketType.STOCK),
            new SeedItem("META", 320.0, MarketType.STOCK),
            new SeedItem("NFLX", 600.0, MarketType.STOCK),
            new SeedItem("AMD", 140.0, MarketType.STOCK),
            new SeedItem("INTC", 45.0, MarketType.STOCK),

            // INDIAN STOCKS
            new SeedItem("RELIANCE", 2900.0, MarketType.STOCK),
            new SeedItem("TCS", 3800.0, MarketType.STOCK),
            new SeedItem("INFY", 1500.0, MarketType.STOCK),
            new SeedItem("HDFCBANK", 1600.0, MarketType.STOCK),
            new SeedItem("ICICIBANK", 1100.0, MarketType.STOCK),
            new SeedItem("SBIN", 750.0, MarketType.STOCK),
            new SeedItem("ITC", 450.0, MarketType.STOCK),
            new SeedItem("HINDUNILVR", 2500.0, MarketType.STOCK),
            new SeedItem("LT", 3500.0, MarketType.STOCK),
            new SeedItem("AXISBANK", 1100.0, MarketType.STOCK),

            // COMMODITIES
            new SeedItem("GOLD", 72000.0, MarketType.COMMODITY),
            new SeedItem("SILVER", 85000.0, MarketType.COMMODITY),
            new SeedItem("CRUDEOIL", 75.0, MarketType.COMMODITY),
            new SeedItem("NATGAS", 3.2, MarketType.COMMODITY),
            new SeedItem("COPPER", 800.0, MarketType.COMMODITY),
            new SeedItem("ALUMINUM", 200.0, MarketType.COMMODITY),
            new SeedItem("ZINC", 250.0, MarketType.COMMODITY),
            new SeedItem("NICKEL", 1500.0, MarketType.COMMODITY),
            new SeedItem("WHEAT", 25.0, MarketType.COMMODITY),
            new SeedItem("CORN", 20.0, MarketType.COMMODITY),

            // EXTRA (SCALING DATA)
            new SeedItem("BTC-USDT", 65000.0, MarketType.CRYPTO),
            new SeedItem("ETH-USDT", 3200.0, MarketType.CRYPTO),
            new SeedItem("SOL-USDT", 140.0, MarketType.CRYPTO),
            new SeedItem("XRP-USDT", 0.6, MarketType.CRYPTO),
            new SeedItem("ADA-USDT", 0.45, MarketType.CRYPTO),
            new SeedItem("DOGE-USDT", 0.12, MarketType.CRYPTO),
            new SeedItem("DOT-USDT", 7.0, MarketType.CRYPTO),
            new SeedItem("MATIC-USDT", 1.0, MarketType.CRYPTO),
            new SeedItem("AVAX-USDT", 35.0, MarketType.CRYPTO),
            new SeedItem("LTC-USDT", 85.0, MarketType.CRYPTO),

            new SeedItem("BANKNIFTY", 48000.0, MarketType.STOCK),
            new SeedItem("NIFTY50", 22000.0, MarketType.STOCK),
            new SeedItem("FINNIFTY", 20000.0, MarketType.STOCK),
            new SeedItem("MIDCPNIFTY", 10000.0, MarketType.STOCK),

            new SeedItem("BRENT", 80.0, MarketType.COMMODITY),
            new SeedItem("PLATINUM", 950.0, MarketType.COMMODITY),
            new SeedItem("PALLADIUM", 1200.0, MarketType.COMMODITY),
            new SeedItem("LEAD", 180.0, MarketType.COMMODITY)
    );

    @Override
    @Transactional
    public void run(String... args) {
        if (marketRepository.count() > 0) {
            log.info("Markets already populated in database. Skipping seed.");
            return;
        }

        log.info("Seeding initial market catalog...");
        int count = 0;

        for (SeedItem item : SEED_ITEMS) {
            String baseAsset;
            String quoteAsset;

            if (item.name().contains("-")) {
                String[] parts = item.name().split("-");
                baseAsset = parts[0];
                quoteAsset = parts[1];
            } else {
                baseAsset = item.name();
                quoteAsset = (item.category() == MarketType.STOCK) ? "INR" : "USD";
            }

            boolean isCrypto = item.category() == MarketType.CRYPTO;
            boolean isStock = item.category() == MarketType.STOCK;

            MarketEntity market = new MarketEntity();
            market.setSymbol(item.name().toUpperCase());
            market.setBaseAsset(baseAsset.toUpperCase());
            market.setQuoteAsset(quoteAsset.toUpperCase());
            market.setType(item.category());
            market.setCategory(item.category().getValue());
            market.setContractType(ContractType.SPOT);
            market.setDisplayName(baseAsset + "/" + quoteAsset);
            market.setLastPrice(item.price());
            market.setVolume24h(0.0);
            market.setStatus(MarketStatus.ACTIVE);
            market.setIsActive(true);
            market.setTickSize(isCrypto ? 0.1 : (isStock ? 0.05 : 1.0));
            market.setLotSize(isCrypto ? 0.0001 : 1.0);
            market.setMinQty(isCrypto ? 0.0001 : 1.0);
            market.setMaxQty(100000.0);
            market.setMinNotional(100.0);
            market.setMaxNotional(100000000.0);
            market.setPricePrecision(2);
            market.setQuantityPrecision(isCrypto ? 6 : 2);
            market.setMakerFee(0.001);
            market.setTakerFee(0.002);

            marketRepository.save(market);
            count++;
        }

        log.info("Seeded {} markets successfully.", count);
    }
}
