package com.toptal.bookshopv2.security;

import com.toptal.bookshopv2.model.User;
import com.toptal.bookshopv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Bridge between the application's {@link User} model and Spring Security's
 * {@link UserDetails} interface.
 *
 * <p>Spring Security requires a {@link UserDetailsService} to load user information
 * during authentication. This implementation loads users from the in-memory
 * {@link UserRepository} and converts them into Spring Security's {@link UserDetails}
 * format, including the role as a granted authority.</p>
 *
 * <h3>Role conversion:</h3>
 * <p>The user's {@link com.toptal.bookshopv2.model.Role} enum value is converted to a
 * Spring Security authority with the {@code ROLE_} prefix:</p>
 * <ul>
 *   <li>{@code Role.USER} → {@code ROLE_USER}</li>
 *   <li>{@code Role.ADMIN} → {@code ROLE_ADMIN}</li>
 * </ul>
 * <p>This prefix is required by Spring Security's {@code hasRole("ADMIN")} which
 * internally checks for the {@code ROLE_ADMIN} authority.</p>
 *
 * <h3>Used by:</h3>
 * <ul>
 *   <li>{@link JwtAuthenticationFilter} — loads user during JWT validation on every request</li>
 *   <li>{@link com.toptal.bookshopv2.config.SecurityConfig} — configured as the
 *       {@code DaoAuthenticationProvider}'s user details service for login authentication</li>
 * </ul>
 *
 * <h3>Important:</h3>
 * <p>The role is always loaded fresh from the repository on each request, ensuring that
 * role changes (e.g., promoting a user to admin) take effect immediately — even if the
 * JWT token still contains the old role in its payload.</p>
 *
 * @author Nitish
 * @version 2.0
 * @see JwtAuthenticationFilter
 * @see com.toptal.bookshopv2.config.SecurityConfig
 * @see UserRepository
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    /** Repository for looking up users by email. */
    private final UserRepository userRepository;

    /**
     * Loads a user by their email address and converts to Spring Security's {@link UserDetails}.
     *
     * <p>This method is called in two contexts:</p>
     * <ol>
     *   <li><strong>Login</strong> — by {@code DaoAuthenticationProvider} to verify credentials</li>
     *   <li><strong>Per-request JWT validation</strong> — by {@link JwtAuthenticationFilter} to
     *       load the user's current authorities after extracting the email from the JWT</li>
     * </ol>
     *
     * @param email the user's email address (used as the username in this system)
     * @return a {@link UserDetails} object containing email, hashed password, and role authority
     * @throws UsernameNotFoundException if no user exists with the given email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }
}
