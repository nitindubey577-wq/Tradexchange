package com.tradexchange.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public class CandleData {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant time;

    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;

    public CandleData() {}

    public CandleData(Instant time, double open, double high, double low, double close, double volume) {
        this.time = time;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public Instant getTime() { return time; }
    public void setTime(Instant time) { this.time = time; }

    public double getOpen() { return open; }
    public void setOpen(double open) { this.open = open; }

    public double getHigh() { return high; }
    public void setHigh(double high) { this.high = high; }

    public double getLow() { return low; }
    public void setLow(double low) { this.low = low; }

    public double getClose() { return close; }
    public void setClose(double close) { this.close = close; }

    public double getVolume() { return volume; }
    public void setVolume(double volume) { this.volume = volume; }
}
