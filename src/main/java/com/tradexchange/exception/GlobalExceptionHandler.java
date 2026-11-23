package com.tradexchange.exception;

import com.tradexchange.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final Set<String> NOT_FOUND_ERRORS = Set.of(
            "ORDER_NOT_FOUND",
            "TRADE_NOT_FOUND",
            "MARKET_NOT_FOUND",
            "USER_NOT_FOUND",
            "ROUTE_NOT_FOUND"
    );

    private static final Set<String> BAD_REQUEST_ERRORS = Set.of(
            "INVALID_REQUEST",
            "USER_ID_REQUIRED",
            "SYMBOL_REQUIRED",
            "TRADE_ID_REQUIRED",
            "ORDER_ID_REQUIRED",
            "TYPE_REQUIRED",
            "INVALID_SIDE",
            "INVALID_TYPE",
            "INVALID_QUANTITY",
            "INVALID_PRICE",
            "INVALID_SYMBOL",
            "INVALID_INTERVAL",
            "MARKET_NOT_AVAILABLE",
            "BELOW_MIN_QTY",
            "ABOVE_MAX_QTY",
            "BELOW_MIN_NOTIONAL",
            "ABOVE_MAX_NOTIONAL",
            "INVALID_QUANTITY_AFTER_ROUND",
            "ORDER_NOT_OPEN"
    );

    @ExceptionHandler(TradingPlatformException.class)
    public ResponseEntity<ApiResponse<Void>> handleTradingPlatformException(TradingPlatformException ex) {
        String code = ex.getErrorCode();

        if (NOT_FOUND_ERRORS.contains(code)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(code));
        }

        if (BAD_REQUEST_ERRORS.contains(code)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(code));
        }

        if ("ORDER_EXECUTION_FAILED".equals(code)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("ENGINE_ERROR"));
        }

        if ("CANCEL_FAILED".equals(code)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("ENGINE_CANCEL_FAILED"));
        }

        log.error("Internal trading platform error: {}", code);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("INTERNAL_SERVER_ERROR"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .findFirst()
                .orElse("INVALID_REQUEST");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("ROUTE_NOT_FOUND"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unhandled exception: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("INTERNAL_SERVER_ERROR"));
    }
}
