package com.toptal.bookshopv2.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Servlet filter that intercepts every HTTP request to validate JWT tokens and
 * establish Spring Security authentication.
 *
 * <p>This filter is registered in the Spring Security filter chain by
 * {@link com.toptal.bookshopv2.config.SecurityConfig} and runs <strong>before</strong>
 * the default {@code UsernamePasswordAuthenticationFilter}.</p>
 *
 * <h3>Processing flow:</h3>
 * <ol>
 *   <li>Extract the {@code Authorization} header from the incoming request</li>
 *   <li>If the header is missing or doesn't start with {@code "Bearer "}, skip and continue the filter chain</li>
 *   <li>Extract the JWT token (everything after {@code "Bearer "})</li>
 *   <li>Use {@link JwtService#extractUsername} to get the email from the token (this also verifies the signature)</li>
 *   <li>If no authentication exists yet in the {@link SecurityContextHolder}:
 *     <ol type="a">
 *       <li>Load the user via {@link CustomUserDetailsService#loadUserByUsername}</li>
 *       <li>Validate the token via {@link JwtService#isTokenValid}</li>
 *       <li>If valid, create a {@link UsernamePasswordAuthenticationToken} and set it in the security context</li>
 *     </ol>
 *   </li>
 *   <li>Continue the filter chain (the request now carries the authenticated principal)</li>
 * </ol>
 *
 * <h3>Error handling:</h3>
 * <p>Any exception during token parsing/validation (expired, malformed, invalid signature)
 * is silently caught. The request proceeds without authentication, and Spring Security's
 * authorization rules will return 401/403 as appropriate.</p>
 *
 * <h3>Extends:</h3>
 * <p>{@link OncePerRequestFilter} ensures this filter executes exactly once per request,
 * even if the request is dispatched multiple times internally (e.g., error handling).</p>
 *
 * @author Nitish
 * @version 2.0
 * @see JwtService
 * @see CustomUserDetailsService
 * @see com.toptal.bookshopv2.config.SecurityConfig
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Service for JWT token operations (extract, validate). */
    private final JwtService jwtService;

    /** Service for loading user details from the in-memory repository. */
    private final CustomUserDetailsService userDetailsService;

    /**
     * Core filter logic: extracts and validates the JWT from the Authorization header,
     * then populates the Spring Security context if the token is valid.
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response (not modified by this filter)
     * @param filterChain the remaining filters to execute after this one
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) { filterChain.doFilter(request, response); return; }
        final String jwt = authHeader.substring(7);
        try {
            final String userEmail = jwtService.extractUsername(jwt);
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) { /* invalid token — request proceeds unauthenticated */ }
        filterChain.doFilter(request, response);
    }
}
