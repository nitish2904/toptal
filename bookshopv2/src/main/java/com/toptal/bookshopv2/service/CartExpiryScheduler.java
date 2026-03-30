package com.toptal.bookshopv2.service;

import com.toptal.bookshopv2.store.DataStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor @Slf4j
public class CartExpiryScheduler {
    private final DataStore dataStore;
    @Value("${app.cart.expiry-minutes}") private long expiryMinutes;
    @Scheduled(fixedRate = 600000)
    public void removeExpiredCartItems() {
        dataStore.deleteExpiredCartItems(expiryMinutes);
        log.debug("Cleaned up expired cart items (older than {} minutes)", expiryMinutes);
    }
}
