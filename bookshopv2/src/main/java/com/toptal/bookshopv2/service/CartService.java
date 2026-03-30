package com.toptal.bookshopv2.service;

import com.toptal.bookshopv2.dto.CartItemResponse;
import com.toptal.bookshopv2.dto.OrderResponse;
import com.toptal.bookshopv2.exception.*;
import com.toptal.bookshopv2.model.*;
import com.toptal.bookshopv2.store.DataStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CartService {
    private final DataStore dataStore;

    public List<CartItemResponse> getCart(String email) {
        User user = getUser(email);
        return dataStore.findCartItemsByUserId(user.getId()).stream()
                .map(ci -> CartItemResponse.from(ci, dataStore.findBookById(ci.getBookId()).orElse(null))).toList();
    }
    public CartItemResponse addToCart(String email, Long bookId) {
        User user = getUser(email);
        Book book = dataStore.findBookById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        if (book.getStock() <= 0) throw new BadRequestException("Book is out of stock");
        if (dataStore.existsCartItemByUserIdAndBookId(user.getId(), bookId))
            throw new ConflictException("Book is already in your cart");
        CartItem ci = dataStore.saveCartItem(CartItem.builder().userId(user.getId()).bookId(bookId).build());
        return CartItemResponse.from(ci, book);
    }
    public void removeFromCart(String email, Long bookId) {
        User user = getUser(email);
        CartItem ci = dataStore.findCartItemByUserIdAndBookId(user.getId(), bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found in your cart"));
        dataStore.deleteCartItem(ci.getId());
    }
    public synchronized OrderResponse checkout(String email) {
        User user = getUser(email);
        List<CartItem> items = dataStore.findCartItemsByUserId(user.getId());
        if (items.isEmpty()) throw new BadRequestException("Cart is empty");
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem ci : items) {
            Book book = dataStore.findBookById(ci.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book no longer exists: " + ci.getBookId()));
            if (book.getStock() <= 0)
                throw new BadRequestException("Book '" + book.getTitle() + "' is no longer in stock. Please remove it from your cart.");
            book.setStock(book.getStock() - 1);
            dataStore.saveBook(book);
            total = total.add(book.getPrice());
            orderItems.add(OrderItem.builder().bookId(book.getId()).priceAtPurchase(book.getPrice()).build());
        }
        Order order = dataStore.saveOrder(Order.builder().userId(user.getId()).totalPrice(total).items(orderItems).build());
        dataStore.deleteCartItemsByUserId(user.getId());
        return OrderResponse.from(order, dataStore.getBookMap());
    }
    public List<OrderResponse> getOrders(String email) {
        User user = getUser(email);
        Map<Long, Book> bookMap = dataStore.getBookMap();
        return dataStore.findOrdersByUserId(user.getId()).stream().map(o -> OrderResponse.from(o, bookMap)).toList();
    }
    private User getUser(String email) {
        return dataStore.findUserByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
