package com.toptal.bookshop.service;

import com.toptal.bookshop.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled task that removes cart items older than the configured expiry time.
 * Runs every 5 minutes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CartExpiryScheduler {

    private final CartItemRepository cartItemRepository;

    @Value("${app.cart.expiry-minutes}")
    private int expiryMinutes;

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional
    public void removeExpiredCartItems() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(expiryMinutes);
        int removed = cartItemRepository.deleteExpiredCartItems(expiryTime);
        if (removed > 0) {
            log.info("Removed {} expired cart items (older than {} minutes)", removed, expiryMinutes);
        }
    }
}
