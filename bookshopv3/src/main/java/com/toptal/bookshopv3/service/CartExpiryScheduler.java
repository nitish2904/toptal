package com.toptal.bookshopv3.service;

import com.toptal.bookshopv3.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
/** Scheduled task that purges cart items older than the configured TTL. */

@Component @RequiredArgsConstructor @Slf4j
public class CartExpiryScheduler {
    private final CartItemRepository cartItemRepository;
    @Value("${app.cart.expiry-minutes}") private long expiryMinutes;
    @Scheduled(fixedRate = 600000)
    public void removeExpiredCartItems() {
        cartItemRepository.deleteExpiredItems(expiryMinutes);
        log.debug("Cleaned up expired cart items (older than {} minutes)", expiryMinutes);
    }
}
