package com.toptal.bookshopv2.config;

import com.toptal.bookshopv2.security.CustomUserDetailsService;
import com.toptal.bookshopv2.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration that defines authentication, authorization, and
 * session management policies for the bookshop API.
 *
 * <h3>Key decisions:</h3>
 * <ul>
 *   <li><strong>Stateless sessions</strong> — no HTTP session; every request must carry a JWT</li>
 *   <li><strong>CSRF disabled</strong> — safe for stateless REST APIs that use Bearer tokens</li>
 *   <li><strong>BCrypt password hashing</strong> — industry-standard adaptive hashing with salt</li>
 * </ul>
 *
 * <h3>Authorization rules (order matters — first match wins):</h3>
 * <table>
 *   <tr><th>Pattern</th><th>Access</th></tr>
 *   <tr><td>{@code /api/auth/**}</td><td>Public (login, register)</td></tr>
 *   <tr><td>{@code GET /api/books/**, GET /api/categories/**}</td><td>Public (catalog browsing)</td></tr>
 *   <tr><td>{@code /api/admin/**}</td><td>ADMIN role only</td></tr>
 *   <tr><td>{@code /api/cart/**, /api/orders/**}</td><td>Any authenticated user</td></tr>
 *   <tr><td>{@code /swagger-ui/**, /v3/api-docs/**}</td><td>Public (API documentation)</td></tr>
 *   <tr><td>Everything else</td><td>Authenticated</td></tr>
 * </table>
 *
 * <h3>Filter chain:</h3>
 * <p>The {@link JwtAuthenticationFilter} is inserted <strong>before</strong>
 * {@link UsernamePasswordAuthenticationFilter} so that JWT-based authentication
 * is attempted first on every request.</p>
 *
 * @author Nitish
 * @version 2.0
 * @see JwtAuthenticationFilter
 * @see CustomUserDetailsService
 */
@Configuration @EnableWebSecurity @RequiredArgsConstructor
public class SecurityConfig {

    /** JWT filter injected into the security filter chain. */
    private final JwtAuthenticationFilter jwtAuthFilter;

    /** User details service for loading users from the in-memory repository. */
    private final CustomUserDetailsService userDetailsService;

    /**
     * Configures the HTTP security filter chain with JWT-based stateless authentication.
     *
     * @param http the {@link HttpSecurity} builder provided by Spring Security
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/books/**", "/api/categories/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/cart/**", "/api/orders/**").authenticated()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Creates a {@link DaoAuthenticationProvider} that uses BCrypt for password verification
     * and our custom {@link CustomUserDetailsService} for user lookup.
     *
     * @return the configured authentication provider
     */
    @Bean public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService); p.setPasswordEncoder(passwordEncoder()); return p;
    }

    /**
     * Exposes the {@link AuthenticationManager} as a Spring bean so it can be injected
     * into {@link com.toptal.bookshopv2.service.AuthService} for login authentication.
     *
     * @param config the authentication configuration provided by Spring
     * @return the authentication manager
     * @throws Exception if retrieval fails
     */
    @Bean public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Creates a BCrypt password encoder bean used for hashing passwords during
     * registration and verifying them during login.
     *
     * <p>BCrypt automatically handles salting and uses an adaptive cost factor
     * (default: 10 rounds = 2^10 iterations).</p>
     *
     * @return a {@link BCryptPasswordEncoder} instance
     */
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
