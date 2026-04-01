package com.toptal.bookshopv2.model;

/**
 * Enumeration representing the authorization roles available in the bookshop system.
 *
 * <p>Roles are used throughout the security layer to enforce access control:</p>
 * <ul>
 *   <li>{@link #USER} — standard customer role; can browse books, manage cart, and place orders</li>
 *   <li>{@link #ADMIN} — administrative role; can create/update/delete books and categories</li>
 * </ul>
 *
 * <p>When stored in the {@link User} model, the role is converted to a Spring Security
 * {@code GrantedAuthority} with the {@code ROLE_} prefix (e.g., {@code ROLE_ADMIN}) by
 * {@link com.toptal.bookshopv2.security.CustomUserDetailsService}.</p>
 *
 * <p>The role is also embedded in the JWT token payload as a custom claim, though
 * authorization decisions always re-load the role from the {@link com.toptal.bookshopv2.repository.UserRepository}
 * to ensure changes take effect immediately.</p>
 *
 * @author Nitish
 * @version 2.0
 * @see User
 * @see com.toptal.bookshopv2.security.CustomUserDetailsService
 * @see com.toptal.bookshopv2.config.SecurityConfig
 */
public enum Role {

    /** Standard customer role. Can browse catalog, manage shopping cart, and place orders. */
    USER,

    /** Administrative role. Can manage books, categories, and has full access to all endpoints. */
    ADMIN
}
