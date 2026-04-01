package com.toptal.bookshopv2.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Service responsible for JWT (JSON Web Token) generation, parsing, and validation.
 *
 * <p>This is the core cryptographic component of the authentication system. It handles:</p>
 * <ul>
 *   <li><strong>Token generation</strong> — creates signed JWTs containing the user's email and role</li>
 *   <li><strong>Token parsing</strong> — extracts claims (email, role, expiry) from a token</li>
 *   <li><strong>Signature verification</strong> — ensures the token was created by this server and hasn't been tampered with</li>
 *   <li><strong>Expiry checking</strong> — rejects tokens that have exceeded their time-to-live</li>
 * </ul>
 *
 * <h3>JWT structure:</h3>
 * <pre>
 * Header:    {"alg": "HS256", "typ": "JWT"}
 * Payload:   {"sub": "user@test.com", "role": "USER", "iat": ..., "exp": ...}
 * Signature: HMAC-SHA256(base64(header) + "." + base64(payload), secretKey)
 * </pre>
 *
 * <h3>Configuration (application.properties):</h3>
 * <ul>
 *   <li>{@code app.jwt.secret} — the HMAC-SHA256 signing key (must be ≥ 256 bits / 32 chars)</li>
 *   <li>{@code app.jwt.expiration-ms} — token time-to-live in milliseconds (e.g., 86400000 = 24 hours)</li>
 * </ul>
 *
 * <h3>Security notes:</h3>
 * <ul>
 *   <li>The payload is base64-encoded, <strong>not encrypted</strong> — never put secrets in it</li>
 *   <li>Signature verification happens inside {@link #extractClaim} via {@code parseSignedClaims()}</li>
 *   <li>If the token is forged or tampered, {@code parseSignedClaims()} throws {@code SignatureException}</li>
 * </ul>
 *
 * @author Nitish
 * @version 2.0
 * @see JwtAuthenticationFilter
 * @see com.toptal.bookshopv2.service.AuthService
 */
@Service
public class JwtService {

    /** HMAC-SHA256 signing secret, injected from {@code app.jwt.secret} property. */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /** Token expiration time in milliseconds, injected from {@code app.jwt.expiration-ms} property. */
    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * Generates a signed JWT token for the given user.
     *
     * <p>The token contains:</p>
     * <ul>
     *   <li>{@code sub} (subject) — the user's email address</li>
     *   <li>{@code role} — the user's role (USER or ADMIN) as a custom claim</li>
     *   <li>{@code iat} (issued at) — current timestamp</li>
     *   <li>{@code exp} (expiration) — current time + {@code jwtExpirationMs}</li>
     * </ul>
     *
     * @param email the user's email address (becomes the JWT subject)
     * @param role  the user's role name (e.g., "USER" or "ADMIN")
     * @return a compact, URL-safe JWT string (e.g., "eyJhbGci...")
     */
    public String generateToken(String email, String role) {
        return Jwts.builder().subject(email).claims(Map.of("role", role))
                .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey()).compact();
    }

    /**
     * Extracts the username (email) from a JWT token.
     *
     * <p>This method also implicitly verifies the token's signature. If the token
     * is forged or tampered with, the underlying {@code parseSignedClaims()} call
     * will throw a {@code SignatureException}.</p>
     *
     * @param token the JWT string to parse
     * @return the email address from the token's {@code sub} claim
     * @throws io.jsonwebtoken.security.SignatureException if the signature is invalid
     * @throws io.jsonwebtoken.ExpiredJwtException if the token has expired
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Validates a JWT token against the provided user details.
     *
     * <p>A token is considered valid if:</p>
     * <ol>
     *   <li>The email in the token matches the user's email</li>
     *   <li>The token has not expired</li>
     * </ol>
     *
     * @param token       the JWT string to validate
     * @param userDetails the user details loaded from {@link CustomUserDetailsService}
     * @return {@code true} if the token is valid for this user, {@code false} otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Extracts a specific claim from the JWT token using a resolver function.
     *
     * <p>This is the central parsing method. It:</p>
     * <ol>
     *   <li>Parses the JWT string</li>
     *   <li>Verifies the HMAC-SHA256 signature using our secret key</li>
     *   <li>Returns the payload claims (only if signature is valid)</li>
     *   <li>Applies the resolver function to extract the desired claim</li>
     * </ol>
     *
     * @param token    the JWT string to parse
     * @param resolver a function that extracts the desired value from {@link Claims}
     * @param <T>      the type of the extracted claim
     * @return the extracted claim value
     */
    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
        return resolver.apply(claims);
    }

    /**
     * Checks whether the token's expiration date is before the current time.
     *
     * @param token the JWT string to check
     * @return {@code true} if the token has expired
     */
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Derives the HMAC-SHA256 signing key from the configured secret string.
     *
     * <p>The secret must be at least 256 bits (32 characters) for HS256.
     * The key is derived fresh on each call (not cached) for simplicity.</p>
     *
     * @return the {@link SecretKey} used for signing and verifying JWTs
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
