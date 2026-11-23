package com.tradexchange.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private Boolean success;
    private String status;
    private Integer count;
    private T data;
    private Object markets;
    private Object watchlist;
    private String orderId;
    private String message;
    private String error;

    public ApiResponse() {}

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setSuccess(true);
        res.setData(data);
        return res;
    }

    public static <T> ApiResponse<T> ok(T data, int count) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setSuccess(true);
        res.setCount(count);
        res.setData(data);
        return res;
    }

    public static ApiResponse<Void> error(String error) {
        ApiResponse<Void> res = new ApiResponse<>();
        res.setSuccess(false);
        res.setError(error);
        return res;
    }

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public Object getMarkets() { return markets; }
    public void setMarkets(Object markets) { this.markets = markets; }

    public Object getWatchlist() { return watchlist; }
    public void setWatchlist(Object watchlist) { this.watchlist = watchlist; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
