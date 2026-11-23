package com.tradexchange.dto.response;

import java.util.List;

public class CandlesResponse {
    private Boolean success;
    private String symbol;
    private String interval;
    private List<CandleData> candles;

    public CandlesResponse() {}

    public CandlesResponse(Boolean success, String symbol, String interval, List<CandleData> candles) {
        this.success = success;
        this.symbol = symbol;
        this.interval = interval;
        this.candles = candles;
    }

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getInterval() { return interval; }
    public void setInterval(String interval) { this.interval = interval; }

    public List<CandleData> getCandles() { return candles; }
    public void setCandles(List<CandleData> candles) { this.candles = candles; }
}
