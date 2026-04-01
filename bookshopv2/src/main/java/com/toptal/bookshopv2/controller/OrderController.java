package com.toptal.bookshopv2.controller;

import com.toptal.bookshopv2.dto.OrderResponse;
import com.toptal.bookshopv2.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for order history (authenticated users).
 *
 * <p>Provides read-only access to the authenticated user's past orders.</p>
 *
 * @author Nitish
 * @version 2.0
 */
@RestController @RequestMapping("/api/orders") @RequiredArgsConstructor
public class OrderController {
    private final CartService cartService;
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(cartService.getOrders(user.getUsername()));
    }
}
