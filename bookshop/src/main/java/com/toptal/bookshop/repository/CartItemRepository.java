package com.toptal.bookshop.repository;

import com.toptal.bookshop.entity.CartItem;
import com.toptal.bookshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
/** Spring Data JPA repository for {@link com.toptal.bookshop.entity.CartItem} with bulk-expiry support. */

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUser(User user);

    Optional<CartItem> findByUserAndBookId(User user, Long bookId);

    boolean existsByUserAndBookId(User user, Long bookId);

    void deleteByUser(User user);

    /**
     * Delete expired cart items (older than the specified time).
     */
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.addedAt < :expiryTime")
    int deleteExpiredCartItems(@Param("expiryTime") LocalDateTime expiryTime);
}
