package com.tradexchange.controller;

import com.tradexchange.dto.request.OrderPlaceRequest;
import com.tradexchange.dto.response.ApiResponse;
import com.tradexchange.engine.OrderBookDepth;
import com.tradexchange.entity.OrderEntity;
import com.tradexchange.enums.OrderStatus;
import com.tradexchange.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Orders", description = "Order placement, retrieval, cancellation and depth endpoints")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    @Operation(summary = "Place a new LIMIT or MARKET order")
    public ResponseEntity<ApiResponse<Void>> placeOrder(@Valid @RequestBody OrderPlaceRequest req) {
        Map<String, Object> result = orderService.placeOrder(req);
        String orderId = (String) result.get("orderId");

        ApiResponse<Void> res = new ApiResponse<>();
        res.setSuccess(true);
        res.setOrderId(orderId);

        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/orders")
    @Operation(summary = "Get all orders with optional filters")
    public ResponseEntity<ApiResponse<List<OrderEntity>>> getAllOrders(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "100") int limit) {

        List<OrderEntity> orders = orderService.getAllOrders(userId, symbol, status, limit);
        ApiResponse<List<OrderEntity>> res = ApiResponse.ok(orders, orders.size());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "Get single order by ID")
    public ResponseEntity<ApiResponse<OrderEntity>> getOrder(@PathVariable String id) {
        OrderEntity order = orderService.getOrder(id);
        return ResponseEntity.ok(ApiResponse.ok(order));
    }

    @DeleteMapping("/orders/{id}")
    @Operation(summary = "Cancel an open order")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cancelOrder(@PathVariable String id) {
        Map<String, Object> result = orderService.cancelOrder(id);

        ApiResponse<Map<String, Object>> res = new ApiResponse<>();
        res.setSuccess(true);
        res.setMessage("ORDER_CANCELLED");
        res.setData(result);

        return ResponseEntity.ok(res);
    }

    @GetMapping("/depth/{symbol}")
    @Operation(summary = "Get aggregated order book market depth")
    public ResponseEntity<ApiResponse<OrderBookDepth>> getDepth(@PathVariable String symbol) {
        OrderBookDepth depth = orderService.getDepth(symbol);
        return ResponseEntity.ok(ApiResponse.ok(depth));
    }
}
