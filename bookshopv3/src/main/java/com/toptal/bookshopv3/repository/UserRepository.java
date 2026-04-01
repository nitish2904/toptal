package com.toptal.bookshopv3.repository;

import com.toptal.bookshopv3.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
/** Spring Data JPA repository for {@link com.toptal.bookshopv3.entity.User} entities. */

@Repository
public class UserRepository {
    private final AtomicLong idGenerator = new AtomicLong(0);
    private final ConcurrentHashMap<Long, User> users = new ConcurrentHashMap<>();

    public User save(User user) {
        if (user.getId() == null) user.setId(idGenerator.incrementAndGet());
        users.put(user.getId(), user);
        return user;
    }

    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public boolean existsByEmail(String email) {
        return users.values().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }
}
