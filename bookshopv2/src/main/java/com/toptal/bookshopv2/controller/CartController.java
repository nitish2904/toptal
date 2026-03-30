package com.toptal.bookshopv2.controller;

import com.toptal.bookshopv2.dto.CartItemResponse;
import com.toptal.bookshopv2.dto.OrderResponse;
import com.toptal.bookshopv2.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/cart") @RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCart(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(cartService.getCart(user.getUsername()));
    }
    @PostMapping("/items/{bookId}")
    public ResponseEntity<CartItemResponse> addToCart(@AuthenticationPrincipal UserDetails user, @PathVariable Long bookId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addToCart(user.getUsername(), bookId));
    }
    @DeleteMapping("/items/{bookId}")
    public ResponseEntity<Void> removeFromCart(@AuthenticationPrincipal UserDetails user, @PathVariable Long bookId) {
        cartService.removeFromCart(user.getUsername(), bookId); return ResponseEntity.noContent().build();
    }
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(cartService.checkout(user.getUsername()));
    }
}
