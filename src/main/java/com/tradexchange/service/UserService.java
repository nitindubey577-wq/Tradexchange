package com.tradexchange.service;

import com.tradexchange.entity.UserEntity;
import com.tradexchange.exception.TradingPlatformException;
import com.tradexchange.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserEntity getOrCreateUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new TradingPlatformException("USER_ID_REQUIRED");
        }

        return userRepository.findById(userId.trim()).orElseGet(() -> {
            UserEntity user = new UserEntity();
            user.setId(userId.trim());
            user.setEmail("user_" + userId.trim() + "@demo.com");
            user.setWatchlist(new LinkedHashSet<>());
            user.setBalances("{}");
            user.setIsActive(true);
            return userRepository.save(user);
        });
    }

    public UserEntity getUserProfile(String userId) {
        return getOrCreateUser(userId);
    }

    public Set<String> getWatchlist(String userId) {
        UserEntity user = getOrCreateUser(userId);
        return user.getWatchlist();
    }

    @Transactional
    public Set<String> toggleWatchlist(String userId, String symbol) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new TradingPlatformException("USER_ID_REQUIRED");
        }
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new TradingPlatformException("INVALID_SYMBOL");
        }

        UserEntity user = getOrCreateUser(userId);
        String normalizedSymbol = symbol.toUpperCase().trim();

        Set<String> watchlist = user.getWatchlist();
        if (watchlist.contains(normalizedSymbol)) {
            watchlist.remove(normalizedSymbol);
        } else {
            watchlist.add(normalizedSymbol);
        }

        userRepository.save(user);
        return watchlist;
    }
}
