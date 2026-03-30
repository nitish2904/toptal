# Toptal Bookshop – Architecture & Security Deep Dive

A RESTful Online Bookshop API built with **Java 21** and **Spring Boot 3.4**. Three versions with different approaches:

| Version | Storage | Auth Mechanism | Key Difference |
|---------|---------|---------------|----------------|
| **bookshop** (v1) | H2 Database + JPA | JWT Bearer Token | Full DB with Spring Data JPA |
| **bookshopv2** | In-Memory HashMap | JWT Bearer Token | No database, pure ConcurrentHashMap |
| **bookshopv3** | In-Memory HashMap | HTTP Basic Auth | No JWT, simplest auth |

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [SecurityConfig – How It Works](#securityconfig--how-it-works)
- [OpenApiConfig – How It Works](#openapiconfig--how-it-works)
- [Authentication Flow: JWT (v2)](#authentication-flow-jwt-v2)
- [Authentication Flow: HTTP Basic (v3)](#authentication-flow-http-basic-v3)
- [Authorization: How Roles Are Checked](#authorization-how-roles-are-checked)
- [Scenario: USER Token → Admin API → 403](#scenario-user-token--admin-api--403)
- [Class Connection Map](#class-connection-map)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     HTTP Request                             │
│   Authorization: Bearer <jwt> (v2) or Basic <b64> (v3)      │
└──────────────────────┬──────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────────┐
│  SecurityFilterChain (from SecurityConfig)                    │
│                                                              │
│  v2: JwtAuthenticationFilter → validates JWT signature       │
│  v3: BasicAuthenticationFilter → checks email:password       │
│                                                              │
│  Then: Authorization rules                                   │
│    /api/auth/**          → permitAll()                       │
│    GET /api/books/**     → permitAll()                       │
│    /api/admin/**         → hasRole("ADMIN")                  │
│    /api/cart/**          → authenticated()                   │
└──────────────────────┬──────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────────┐
│  Controllers                                                  │
│  AuthController → BookController → CartController → etc.     │
│                       ↓                                       │
│  Services                                                     │
│  AuthService → BookService → CartService → CategoryService   │
│                       ↓                                       │
│  Repositories (In-Memory ConcurrentHashMap)                  │
│  UserRepository → BookRepository → CartItemRepository → etc. │
└──────────────────────────────────────────────────────────────┘
```

---

## SecurityConfig – How It Works

### What does it do?

`SecurityConfig` is the **gatekeeper** of the entire application. It defines:
- **How users authenticate** (JWT or HTTP Basic)
- **Which URLs are public** and which need auth
- **Which roles** can access which endpoints

### Who uses it? (The invisible wiring)

**No class in your code explicitly calls SecurityConfig.** Spring Boot does it automatically:

1. `@SpringBootApplication` triggers component scanning
2. Finds `@Configuration @EnableWebSecurity` on SecurityConfig
3. Creates beans and installs them into the servlet filter chain

```
SecurityConfig creates these beans:
├── SecurityFilterChain    → installed as servlet filter (intercepts ALL requests)
├── AuthenticationProvider → plugged into Spring Security's auth system
│     ├── CustomUserDetailsService → loads users from UserRepository
│     └── BCryptPasswordEncoder    → hashes/verifies passwords
├── AuthenticationManager  → injected into AuthService
└── PasswordEncoder        → injected into AuthService
```

### Key difference between v2 and v3

**v2 (JWT):**
```java
.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
// Custom filter runs BEFORE Spring's default auth filter
```

**v3 (HTTP Basic):**
```java
.httpBasic(Customizer.withDefaults())
// Uses Spring's built-in BasicAuthenticationFilter – no custom filter needed
```

### URL Authorization Matrix

| URL Pattern | Rule | Effect |
|-------------|------|--------|
| `/api/auth/register` | `permitAll()` | Anyone, no auth needed |
| `GET /api/books/**` | `permitAll()` | Browse catalog publicly |
| `GET /api/categories/**` | `permitAll()` | Browse categories publicly |
| `/api/admin/**` | `hasRole("ADMIN")` | Only ADMIN role |
| `/api/cart/**` | `authenticated()` | Any logged-in user |
| `/api/orders/**` | `authenticated()` | Any logged-in user |
| `/swagger-ui/**` | `permitAll()` | API docs accessible |

---

## OpenApiConfig – How It Works

### What does it do?

Configures **Swagger/OpenAPI** – the auto-generated interactive API documentation at `/swagger-ui.html`.

### Who uses it?

The `springdoc-openapi` library (from `pom.xml`) does:
1. Auto-scans all `@RestController` classes → finds endpoints
2. Finds the `@Bean OpenAPI` from OpenApiConfig → merges security metadata
3. Generates JSON at `/v3/api-docs`
4. Swagger UI renders it as an interactive page

**No class in your code references OpenApiConfig.** It's consumed entirely by the springdoc library.

### What it adds

- **API title/version/description** shown at the top of Swagger UI
- **Security scheme** (Basic or Bearer JWT) → adds the "Authorize" button
- **Without OpenApiConfig:** Swagger still works but has no auth button and no title

---

## Authentication Flow: JWT (v2)

JWT has **two phases**: token creation (login) and token verification (per request).

### Phase 1: Login → Get Token

```
POST /api/auth/login  {"email":"user@test.com", "password":"pass123"}
         ↓
┌─ AuthController.login() ─────────────────────────────────────┐
│         ↓                                                     │
│  AuthService.login(request)                                   │
│    ├── authenticationManager.authenticate(email, password)    │
│    │     ├── DaoAuthenticationProvider         [SecurityConfig]│
│    │     │     ├── CustomUserDetailsService                   │
│    │     │     │     └── UserRepository.findByEmail()         │
│    │     │     └── BCryptPasswordEncoder.matches()            │
│    │     └── Password correct? ✓                              │
│    ├── jwtService.generateToken(email, role)                  │
│    │     └── Jwts.builder()                                   │
│    │           .subject("user@test.com")                      │
│    │           .claims({role: "USER"})                        │
│    │           .signWith(secretKey)  ← HMAC-SHA256            │
│    │           .compact()                                     │
│    └── return {token: "eyJ...", email, role}                  │
└───────────────────────────────────────────────────────────────┘
```

The JWT token contains:
```
Header:  {"alg":"HS256"}
Payload: {"sub":"user@test.com", "role":"USER", "iat":..., "exp":...}
Signature: HMAC-SHA256(header.payload, secretKey)
```

### Phase 2: Subsequent Requests → Token Validation

```
POST /api/cart/items/1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
         ↓
┌─ JwtAuthenticationFilter.doFilterInternal() ─────────────────┐
│                                                               │
│  ① Extract "Authorization: Bearer eyJ..." header             │
│  ② jwt = header.substring(7)                                 │
│  ③ jwtService.extractUsername(jwt)                            │
│       └── Jwts.parser().verifyWith(secretKey)                 │
│             .parseSignedClaims(jwt)                           │
│             → SIGNATURE VERIFIED ✓                            │
│             → email = "user@test.com"                         │
│  ④ userDetailsService.loadUserByUsername("user@test.com")     │
│       └── UserRepository → returns user with ROLE_USER        │
│  ⑤ jwtService.isTokenValid(jwt, userDetails)                 │
│       └── email matches ✓ AND not expired ✓                   │
│  ⑥ SecurityContextHolder.setAuthentication(                   │
│       user=user@test.com, authorities=["ROLE_USER"])          │
│                                                               │
└───────────────────────┬───────────────────────────────────────┘
                        ↓
┌─ SecurityFilterChain Authorization ──────────────────────────┐
│  /api/cart/** → authenticated() → YES ✓ → proceed            │
└───────────────────────┬──────────────────────────────────────┘
                        ↓
              CartController.addToCart()
```

**Important:** The role in the JWT payload (`"role":"USER"`) is **NOT used for authorization**. The role is loaded from `UserRepository` via `CustomUserDetailsService`. This ensures role changes take effect immediately.

---

## Authentication Flow: HTTP Basic (v3)

HTTP Basic sends credentials with **every request**. No login endpoint needed.

```
POST /api/cart/items/1
Authorization: Basic dXNlckB0ZXN0LmNvbTpwYXNzMTIz    (base64 of "user@test.com:pass123")
         ↓
┌─ Spring's BasicAuthenticationFilter (built-in) ──────────────┐
│                                                               │
│  ① Extract + decode "Authorization: Basic ..." header         │
│       → email = "user@test.com", password = "pass123"         │
│  ② DaoAuthenticationProvider                  [SecurityConfig]│
│       ├── CustomUserDetailsService                            │
│       │     └── UserRepository.findByEmail("user@test.com")   │
│       └── BCryptPasswordEncoder.matches("pass123", hash)      │
│            → matches ✓                                        │
│  ③ SecurityContextHolder.setAuthentication(                   │
│       user=user@test.com, authorities=["ROLE_USER"])          │
│                                                               │
└───────────────────────┬───────────────────────────────────────┘
                        ↓
┌─ SecurityFilterChain Authorization ──────────────────────────┐
│  /api/cart/** → authenticated() → YES ✓ → proceed            │
└───────────────────────┬──────────────────────────────────────┘
                        ↓
              CartController.addToCart()
```

### JWT vs Basic – Comparison

| Aspect | JWT (v2) | HTTP Basic (v3) |
|--------|----------|-----------------|
| Login | `POST /api/auth/login` → get token | Not needed |
| Per-request header | `Authorization: Bearer <token>` | `Authorization: Basic base64(email:pass)` |
| Password sent | Only at login | With EVERY request |
| Server work per request | Verify signature (CPU only) + DB lookup | BCrypt compare + DB lookup |
| Custom filter | YES – `JwtAuthenticationFilter` | NO – Spring's built-in filter |
| Token expiry | Built-in (`exp` claim) | No concept of expiry |

---

## Authorization: How Roles Are Checked

### Where does the role come from?

The role is **always loaded from `CustomUserDetailsService`**, regardless of JWT or Basic auth:

```java
// CustomUserDetailsService.java:
public UserDetails loadUserByUsername(String email) {
    User user = userRepository.findByEmail(email).orElseThrow();
    return new User(user.getEmail(), user.getPassword(),
        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        //                                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^
        //                                  "ROLE_USER" or "ROLE_ADMIN"
}
```

### How SecurityConfig checks roles

```java
// SecurityConfig.java:
.requestMatchers("/api/admin/**").hasRole("ADMIN")
```

Spring internally:
1. `hasRole("ADMIN")` → looks for authority `"ROLE_ADMIN"` (auto-adds `ROLE_` prefix)
2. Gets authorities from `SecurityContextHolder.getContext().getAuthentication().getAuthorities()`
3. Checks if it contains `"ROLE_ADMIN"`

---

## Scenario: USER Token → Admin API → 403

**What happens when a regular user tries to access an admin endpoint?**

```
POST /api/admin/books
Authorization: Bearer eyJ...{sub:"user@test.com", role:"USER"}...
```

### Step-by-step code execution:

```
┌─ JwtAuthenticationFilter ────────────────────────────────────┐
│                                                               │
│  ① Extract JWT from header                           ✓       │
│  ② Verify signature with secret key                  ✓       │
│       (token IS genuine, signed by our server)                │
│  ③ Extract email: "user@test.com"                    ✓       │
│  ④ Load user from UserRepository → role = ROLE_USER  ✓       │
│  ⑤ Token not expired?                                ✓       │
│  ⑥ Set SecurityContext:                                       │
│       user = user@test.com                                    │
│       authorities = ["ROLE_USER"]                             │
│                                                               │
│  ✓ AUTHENTICATION PASSED (user is who they claim to be)      │
└───────────────────────┬───────────────────────────────────────┘
                        ↓
┌─ SecurityFilterChain Authorization ──────────────────────────┐
│                                                               │
│  URL: /api/admin/books                                        │
│  Matching rule: .requestMatchers("/api/admin/**")             │
│                  .hasRole("ADMIN")                            │
│                                                               │
│  Check: does ["ROLE_USER"] contain "ROLE_ADMIN"?              │
│         → NO                                                  │
│                                                               │
│  ✗ AUTHORIZATION FAILED → AccessDeniedException              │
└───────────────────────┬───────────────────────────────────────┘
                        ↓
                  403 Forbidden
                  {
                    "status": 403,
                    "error": "Forbidden",
                    "path": "/api/admin/books"
                  }

┌─ AdminController.createBook() ───────────────────────────────┐
│                                                               │
│  ❌ NEVER REACHED                                             │
└───────────────────────────────────────────────────────────────┘
```

### Key Distinction

| Concept | Meaning | Result in this scenario |
|---------|---------|----------------------|
| **Authentication** | "Are you who you claim to be?" | ✓ PASSED – valid JWT, real user |
| **Authorization** | "Are you allowed to do this?" | ✗ FAILED – ROLE_USER ≠ ROLE_ADMIN |

---

## Class Connection Map

### How classes are wired together (Spring Dependency Injection)

```
SecurityConfig
  ├── @Bean SecurityFilterChain     → [Tomcat Servlet Filter] intercepts ALL requests
  ├── @Bean AuthenticationProvider
  │     ├── uses: CustomUserDetailsService (@Service)
  │     │           └── uses: UserRepository (@Component)
  │     └── uses: PasswordEncoder (@Bean BCrypt)
  ├── @Bean AuthenticationManager   → injected into AuthService
  └── @Bean PasswordEncoder         → injected into AuthService

AuthService (@Service)
  ├── uses: UserRepository          → save/find users
  ├── uses: PasswordEncoder         → from SecurityConfig @Bean
  ├── uses: JwtService (v2 only)    → generate tokens
  └── uses: AuthenticationManager   → from SecurityConfig @Bean (v2 login)

JwtAuthenticationFilter (v2 only, @Component)
  ├── uses: JwtService              → extract email, validate token
  └── uses: CustomUserDetailsService → load user details

OpenApiConfig
  └── @Bean OpenAPI                 → consumed by springdoc-openapi library (from pom.xml)
                                      generates /v3/api-docs + /swagger-ui.html

Controllers (don't reference any config class)
  ├── AuthController   → AuthService
  ├── AdminController  → BookService, CategoryService
  ├── BookController   → BookService
  ├── CartController   → CartService
  ├── OrderController  → CartService
  └── CategoryController → CategoryService
```

**The key insight:** Controllers never import or reference SecurityConfig. The security filter chain runs *before* any controller method is called – like a security checkpoint at an airport. Gate agents (controllers) don't need to know about the checkpoint.

---

## Quick Start

```bash
# v2 (JWT)
cd bookshopv2 && ./mvnw spring-boot:run

# v3 (HTTP Basic)
cd bookshopv3 && ./mvnw spring-boot:run
```

Default admin: `admin@bookshop.com` / `admin123`

Swagger: http://localhost:8080/swagger-ui.html
