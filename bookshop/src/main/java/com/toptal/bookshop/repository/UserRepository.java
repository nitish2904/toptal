package com.toptal.bookshop.repository;

import com.toptal.bookshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
/** Spring Data JPA repository for {@link com.toptal.bookshop.entity.User} entities. */

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
