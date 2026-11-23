package com.tradexchange.controller;

import com.tradexchange.dto.request.WatchlistToggleRequest;
import com.tradexchange.dto.response.ApiResponse;
import com.tradexchange.entity.UserEntity;
import com.tradexchange.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "User", description = "User profile and watchlist endpoints")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get user profile")
    public ResponseEntity<ApiResponse<UserEntity>> getUserProfile(
            @RequestParam(defaultValue = "user_default") String userId) {
        UserEntity user = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @GetMapping("/watchlist")
    @Operation(summary = "Get user watchlist")
    public ResponseEntity<ApiResponse<Set<String>>> getWatchlist(
            @RequestParam(defaultValue = "user_default") String userId) {
        Set<String> watchlist = userService.getWatchlist(userId);

        ApiResponse<Set<String>> res = new ApiResponse<>();
        res.setSuccess(true);
        res.setData(watchlist);
        res.setWatchlist(watchlist);

        return ResponseEntity.ok(res);
    }

    @PostMapping("/watchlist")
    @Operation(summary = "Toggle a market symbol in user watchlist")
    public ResponseEntity<ApiResponse<Set<String>>> toggleWatchlist(
            @RequestBody WatchlistToggleRequest req) {

        String userId = (req.getUserId() != null && !req.getUserId().trim().isEmpty())
                ? req.getUserId().trim()
                : "user_default";

        Set<String> watchlist = userService.toggleWatchlist(userId, req.getSymbol());

        ApiResponse<Set<String>> res = new ApiResponse<>();
        res.setSuccess(true);
        res.setData(watchlist);
        res.setWatchlist(watchlist);

        return ResponseEntity.ok(res);
    }
}
