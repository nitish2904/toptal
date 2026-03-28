package com.toptal.bookshop.service;

import com.toptal.bookshop.dto.CartItemResponse;
import com.toptal.bookshop.dto.OrderResponse;
import com.toptal.bookshop.entity.*;
import com.toptal.bookshop.exception.BadRequestException;
import com.toptal.bookshop.exception.ConflictException;
import com.toptal.bookshop.exception.ResourceNotFoundException;
import com.toptal.bookshop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public List<CartItemResponse> getCart(String email) {
        User user = getUser(email);
        return cartItemRepository.findByUser(user).stream()
                .map(CartItemResponse::from)
                .toList();
    }

    @Transactional
    public CartItemResponse addToCart(String email, Long bookId) {
        User user = getUser(email);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        if (book.getStock() <= 0) {
            throw new BadRequestException("Book is out of stock");
        }

        if (cartItemRepository.existsByUserAndBookId(user, bookId)) {
            throw new ConflictException("Book is already in your cart");
        }

        CartItem cartItem = CartItem.builder()
                .user(user)
                .book(book)
                .build();

        cartItem = cartItemRepository.save(cartItem);
        return CartItemResponse.from(cartItem);
    }

    @Transactional
    public void removeFromCart(String email, Long bookId) {
        User user = getUser(email);
        CartItem cartItem = cartItemRepository.findByUserAndBookId(user, bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found in your cart"));
        cartItemRepository.delete(cartItem);
    }

    /**
     * Checkout: purchase all books in the cart.
     * Uses pessimistic locking to prevent race conditions.
     */
    @Transactional
    public OrderResponse checkout(String email) {
        User user = getUser(email);
        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            // Lock the book row to prevent race conditions
            Book book = bookRepository.findByIdWithLock(cartItem.getBook().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Book no longer exists: " + cartItem.getBook().getTitle()));

            if (book.getStock() <= 0) {
                throw new BadRequestException(
                        "Book '" + book.getTitle() + "' is no longer in stock. Please remove it from your cart.");
            }

            // Decrement stock
            book.setStock(book.getStock() - 1);
            bookRepository.save(book);

            totalPrice = totalPrice.add(book.getPrice());

            OrderItem orderItem = OrderItem.builder()
                    .book(book)
                    .priceAtPurchase(book.getPrice())
                    .build();
            orderItems.add(orderItem);
        }

        // Create order
        Order order = Order.builder()
                .user(user)
                .totalPrice(totalPrice)
                .build();

        // Link order items to order
        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.setItems(orderItems);

        order = orderRepository.save(order);

        // Clear the cart
        cartItemRepository.deleteByUser(user);

        return OrderResponse.from(order);
    }

    public List<OrderResponse> getOrders(String email) {
        User user = getUser(email);
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(OrderResponse::from)
                .toList();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
