package com.toptal.bookshopv2.store;

import com.toptal.bookshopv2.model.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class DataStore {
    private final AtomicLong userIdGen = new AtomicLong(0);
    private final AtomicLong categoryIdGen = new AtomicLong(0);
    private final AtomicLong bookIdGen = new AtomicLong(0);
    private final AtomicLong cartItemIdGen = new AtomicLong(0);
    private final AtomicLong orderIdGen = new AtomicLong(0);
    private final AtomicLong orderItemIdGen = new AtomicLong(0);

    private final ConcurrentHashMap<Long, User> users = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Category> categories = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Book> books = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, CartItem> cartItems = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Order> orders = new ConcurrentHashMap<>();

    // USERS
    public User saveUser(User user) {
        if (user.getId() == null) user.setId(userIdGen.incrementAndGet());
        users.put(user.getId(), user);
        return user;
    }
    public Optional<User> findUserByEmail(String email) {
        return users.values().stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst();
    }
    public boolean existsUserByEmail(String email) {
        return users.values().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }

    // CATEGORIES
    public Category saveCategory(Category c) {
        if (c.getId() == null) c.setId(categoryIdGen.incrementAndGet());
        categories.put(c.getId(), c);
        return c;
    }
    public Optional<Category> findCategoryById(Long id) { return Optional.ofNullable(categories.get(id)); }
    public List<Category> findAllCategories() { return new ArrayList<>(categories.values()); }
    public boolean existsCategoryByName(String name) {
        return categories.values().stream().anyMatch(c -> c.getName().equalsIgnoreCase(name));
    }
    public boolean existsCategoryByNameAndNotId(String name, Long id) {
        return categories.values().stream().anyMatch(c -> c.getName().equalsIgnoreCase(name) && !c.getId().equals(id));
    }
    public void deleteCategory(Long id) { categories.remove(id); }

    // BOOKS
    public Book saveBook(Book b) {
        if (b.getId() == null) b.setId(bookIdGen.incrementAndGet());
        books.put(b.getId(), b);
        return b;
    }
    public Optional<Book> findBookById(Long id) { return Optional.ofNullable(books.get(id)); }
    public List<Book> findInStockBooks() {
        return books.values().stream().filter(b -> b.getStock() != null && b.getStock() > 0).collect(Collectors.toList());
    }
    public List<Book> findInStockBooksByCategoryIds(List<Long> ids) {
        return books.values().stream().filter(b -> b.getStock() != null && b.getStock() > 0)
                .filter(b -> ids.contains(b.getCategoryId())).collect(Collectors.toList());
    }
    public void deleteBook(Long id) { books.remove(id); }
    public boolean existsBookByCategoryId(Long catId) {
        return books.values().stream().anyMatch(b -> catId.equals(b.getCategoryId()));
    }

    // CART
    public CartItem saveCartItem(CartItem ci) {
        if (ci.getId() == null) ci.setId(cartItemIdGen.incrementAndGet());
        cartItems.put(ci.getId(), ci);
        return ci;
    }
    public List<CartItem> findCartItemsByUserId(Long userId) {
        return cartItems.values().stream().filter(ci -> ci.getUserId().equals(userId)).collect(Collectors.toList());
    }
    public boolean existsCartItemByUserIdAndBookId(Long userId, Long bookId) {
        return cartItems.values().stream().anyMatch(ci -> ci.getUserId().equals(userId) && ci.getBookId().equals(bookId));
    }
    public Optional<CartItem> findCartItemByUserIdAndBookId(Long userId, Long bookId) {
        return cartItems.values().stream().filter(ci -> ci.getUserId().equals(userId) && ci.getBookId().equals(bookId)).findFirst();
    }
    public void deleteCartItem(Long id) { cartItems.remove(id); }
    public void deleteCartItemsByUserId(Long userId) {
        cartItems.entrySet().removeIf(e -> e.getValue().getUserId().equals(userId));
    }
    public void deleteExpiredCartItems(long expiryMinutes) {
        var cutoff = java.time.LocalDateTime.now().minusMinutes(expiryMinutes);
        cartItems.entrySet().removeIf(e -> e.getValue().getAddedAt().isBefore(cutoff));
    }

    // ORDERS
    public Order saveOrder(Order order) {
        if (order.getId() == null) order.setId(orderIdGen.incrementAndGet());
        for (OrderItem item : order.getItems()) {
            if (item.getId() == null) item.setId(orderItemIdGen.incrementAndGet());
            item.setOrderId(order.getId());
        }
        orders.put(order.getId(), order);
        return order;
    }
    public List<Order> findOrdersByUserId(Long userId) {
        return orders.values().stream().filter(o -> o.getUserId().equals(userId))
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed()).collect(Collectors.toList());
    }
    public Map<Long, Book> getBookMap() { return Collections.unmodifiableMap(books); }
}
