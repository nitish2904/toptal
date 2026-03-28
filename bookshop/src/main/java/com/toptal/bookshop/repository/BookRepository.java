package com.toptal.bookshop.repository;

import com.toptal.bookshop.entity.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Find all in-stock books (for public listing).
     */
    Page<Book> findByStockGreaterThan(int stock, Pageable pageable);

    /**
     * Find in-stock books filtered by category IDs.
     */
    @Query("SELECT b FROM Book b WHERE b.stock > 0 AND b.category.id IN :categoryIds")
    Page<Book> findInStockByCategoryIds(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);

    /**
     * Lock a book row for update (used during checkout to prevent race conditions).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdWithLock(@Param("id") Long id);
}
