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
- [Swagger/OpenAPI – How It Works & Setup Guide](#swaggeropenapi--how-it-works--setup-guide)
- [JWT Deep Dive – What, Why, How & Security Analysis](#jwt-deep-dive--what-why-how--security-analysis)
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

## Swagger/OpenAPI – How It Works & Setup Guide

### What is Swagger?

**Swagger** (now called **OpenAPI**) is an auto-generated, interactive documentation page for your REST API. It:
- Lists all endpoints with their HTTP methods, URLs, request/response formats
- Lets you **try out** API calls directly from the browser (no curl/Postman needed)
- Auto-generates from your Java code – no manual documentation writing

### What you see

When you visit `http://localhost:8080/swagger-ui.html`:

```
┌─────────────────────────────────────────────────────────────┐
│  Bookshop API v3                              [Authorize 🔒] │
│  Online Bookshop REST API – HTTP Basic Auth                  │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ▸ auth-controller                                           │
│    POST /api/auth/register    Register a new user            │
│                                                              │
│  ▸ book-controller                                           │
│    GET  /api/books            List all books (paginated)     │
│    GET  /api/books/{id}       Get book by ID                 │
│                                                              │
│  ▸ admin-controller                                          │
│    POST /api/admin/books      Create book        🔒          │
│    PUT  /api/admin/books/{id} Update book        🔒          │
│    ...                                                       │
│                                                              │
│  ▸ cart-controller                                           │
│    GET  /api/cart             View cart           🔒          │
│    POST /api/cart/items/{id}  Add to cart         🔒          │
│    ...                                                       │
└─────────────────────────────────────────────────────────────┘
```

### How to Set Up Swagger in Any Spring Boot App

There are **3 levels** of setup, from minimal to full:

#### Level 1: Just the Dependency (Minimal – Works Immediately)

Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.6</version>
</dependency>
```

**That's it.** No config class, no annotations. Swagger UI appears at `/swagger-ui.html` and auto-discovers all your `@RestController` endpoints.

What springdoc does automatically:
- Scans all `@RestController` classes
- Reads `@GetMapping`, `@PostMapping`, `@RequestMapping`, etc. → extracts URL paths + HTTP methods
- Reads `@RequestBody` DTO classes → generates request schemas
- Reads return types → generates response schemas
- Reads `@Valid` + Jakarta validation annotations (`@NotBlank`, `@Min`, etc.) → shows constraints
- Serves raw JSON spec at `GET /v3/api-docs`
- Serves Swagger UI at `GET /swagger-ui.html`

#### Level 2: Add OpenApiConfig (Recommended – Adds Auth & Metadata)

Create `config/OpenApiConfig.java`:
```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API metadata (title, version, description)
                .info(new Info()
                    .title("Bookshop API v3")
                    .version("3.0")
                    .description("Online Bookshop REST API"))
                // Security: what auth scheme the API uses
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
                .schemaRequirement("basicAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("basic"));  // or "bearer" for JWT
    }
}
```

This adds:
- **Title/version/description** at the top of Swagger UI
- **"Authorize" button** → users click it to enter credentials
- **🔒 lock icon** on endpoints that require auth

#### Level 3: Per-Endpoint Annotations (Advanced – Fine-Grained Docs)

Add annotations on individual controller methods for detailed docs:
```java
@Operation(summary = "Create a book", description = "Admin only. Creates a new book in the catalog.")
@ApiResponses({
    @ApiResponse(responseCode = "201", description = "Book created"),
    @ApiResponse(responseCode = "400", description = "Validation error"),
    @ApiResponse(responseCode = "403", description = "Not an admin")
})
@PostMapping("/api/admin/books")
public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) { ... }
```

> **Our project uses Level 2** – dependency + OpenApiConfig. We don't use Level 3 annotations because the auto-generated docs are sufficient.

### With Spring Security: The Critical SecurityConfig Rule

If you use Spring Security, you **must** allow public access to Swagger URLs, otherwise Swagger UI itself will require authentication:

```java
// SecurityConfig.java – THIS IS REQUIRED:
.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
```

Without this rule, navigating to `/swagger-ui.html` returns 401/403.

### How Swagger Works Internally

```
┌─ At Startup ─────────────────────────────────────────────────┐
│                                                               │
│  springdoc-openapi-starter-webmvc-ui (from pom.xml)          │
│    ↓                                                          │
│  Auto-configures itself via Spring Boot auto-configuration    │
│    ↓                                                          │
│  Scans all @RestController classes                            │
│    ├── @GetMapping("/api/books") → GET /api/books             │
│    ├── @PostMapping("/api/admin/books") → POST /api/admin/... │
│    ├── @RequestBody BookRequest → reads fields, types, valid. │
│    └── return BookResponse → reads response schema            │
│    ↓                                                          │
│  Finds @Bean OpenAPI (from OpenApiConfig)                     │
│    ├── Merges title, version, description                     │
│    └── Merges security scheme (Basic/Bearer)                  │
│    ↓                                                          │
│  Registers endpoints:                                         │
│    GET /v3/api-docs      → serves OpenAPI JSON spec           │
│    GET /swagger-ui.html  → serves Swagger UI (static HTML/JS)│
│    GET /swagger-ui/**    → serves Swagger UI assets           │
│                                                               │
└───────────────────────────────────────────────────────────────┘

┌─ At Runtime (when you visit /swagger-ui.html) ───────────────┐
│                                                               │
│  Browser loads /swagger-ui.html                               │
│    ↓                                                          │
│  Swagger UI JavaScript fetches /v3/api-docs                   │
│    ↓                                                          │
│  Gets JSON like:                                              │
│  {                                                            │
│    "openapi": "3.0",                                          │
│    "info": {"title": "Bookshop API v3", ...},                 │
│    "paths": {                                                 │
│      "/api/books": {                                          │
│        "get": {                                               │
│          "parameters": [{"name":"page"}, {"name":"size"}],    │
│          "responses": {"200": {"schema": "PageBookResponse"}} │
│        }                                                      │
│      },                                                       │
│      "/api/admin/books": {                                    │
│        "post": {                                              │
│          "requestBody": {"schema": "BookRequest"},            │
│          "security": [{"basicAuth": []}]                      │
│        }                                                      │
│      }                                                        │
│    },                                                         │
│    "components": {                                            │
│      "schemas": {                                             │
│        "BookRequest": {                                       │
│          "title": {"type":"string", "required": true},        │
│          "price": {"type":"number", "minimum": 0.01}          │
│        }                                                      │
│      },                                                       │
│      "securitySchemes": {                                     │
│        "basicAuth": {"type":"http", "scheme":"basic"}         │
│      }                                                        │
│    }                                                          │
│  }                                                            │
│    ↓                                                          │
│  Renders interactive UI with "Try it out" buttons             │
│                                                               │
└───────────────────────────────────────────────────────────────┘
```

### Security Schemes for Different Auth Types

**HTTP Basic (v3):**
```java
new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")
// Swagger UI shows: Username [____] Password [____]
```

**JWT Bearer (v2):**
```java
new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
// Swagger UI shows: Value [________________] (paste JWT token)
```

**API Key:**
```java
new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name("X-API-Key")
// Swagger UI shows: API Key [________________]
```

### Checklist: Setting Up Swagger in a New Spring Boot Project

| Step | Required? | What to do |
|------|-----------|------------|
| 1. Add dependency | ✅ Required | Add `springdoc-openapi-starter-webmvc-ui` to `pom.xml` |
| 2. OpenApiConfig | 📋 Recommended | Create `@Configuration` class with `@Bean OpenAPI` for title + auth |
| 3. SecurityConfig rule | ✅ Required (if using Spring Security) | Add `.permitAll()` for `/swagger-ui/**`, `/v3/api-docs/**` |
| 4. Per-endpoint annotations | ⭐ Optional | Add `@Operation`, `@ApiResponses` on controller methods |
| 5. Customize properties | ⭐ Optional | Set `springdoc.swagger-ui.path=/custom-path` in `application.properties` |

---

## JWT Deep Dive – What, Why, How & Security Analysis

### What is JWT?

**JWT (JSON Web Token)** is an open standard (RFC 7519) for securely transmitting information between parties as a compact, self-contained JSON object. It's digitally signed, so it can be **verified and trusted**.

Think of it like a tamper-proof ID card:
- The bouncer (server) issues you an ID card (JWT) at the door (login)
- The card has your name and VIP status written on it (claims)
- It has a holographic seal (signature) that only the bouncer can create
- Inside the venue, security just checks the seal – no need to call the front desk

### JWT Structure – The Three Parts

A JWT looks like this:
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQHRlc3QuY29tIiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3NDMzNjYwMDAsImV4cCI6MTc0MzQ1MjQwMH0.K7x9f2mN...
|_____ HEADER _______|._________________ PAYLOAD __________________|.__ SIGNATURE __|
```

**Part 1: Header** (algorithm + type)
```json
{
  "alg": "HS256",    // HMAC-SHA256 signing algorithm
  "typ": "JWT"       // token type
}
```

**Part 2: Payload** (claims – the actual data)
```json
{
  "sub": "user@test.com",    // subject – WHO this token is for
  "role": "USER",            // custom claim – user's role
  "iat": 1743366000,         // issued at – WHEN it was created
  "exp": 1743452400          // expiration – WHEN it expires
}
```

**Part 3: Signature** (tamper protection)
```
HMAC-SHA256(
  base64(header) + "." + base64(payload),
  secretKey    // only our server knows this
)
```

The header and payload are just **base64-encoded** (not encrypted!) – anyone can decode and read them. The **signature** is what provides security: it proves the token was created by our server and hasn't been tampered with.

### Why JWT? The Problem It Solves

**The traditional approach (sessions):**
```
Client                      Server
  ├── POST /login ──────────→ Create session in memory/DB
  │                            Store: sessionId → {user, role}
  ←── Cookie: sessionId ────┤
  │                            
  ├── GET /api/cart ─────────→ Look up sessionId in memory/DB
  │   Cookie: sessionId        Find user → allow
  ←── {cart items} ─────────┤
```
**Problem:** Server must store sessions. With 3 servers behind a load balancer, if Server A created the session but Server B gets the next request → user is "not logged in". Requires sticky sessions or shared session store (Redis).

**The JWT approach (stateless):**
```
Client                      Server
  ├── POST /login ──────────→ Verify password
  │                            Generate JWT (signed with secret)
  ←── {token: "eyJ..."} ───┤  Nothing stored on server!
  │                            
  ├── GET /api/cart ─────────→ Verify JWT signature
  │   Bearer: eyJ...           Extract user from token → allow
  ←── {cart items} ─────────┤  No DB lookup for session!
```
**Solution:** Server stores NOTHING. The token itself carries all the info. Any server can verify it with the secret key.

### How JWT Fits Our Bookshop (v2)

In our project, JWT serves as a **stateless authentication mechanism**:

1. User registers/logs in → server generates JWT containing email + role
2. Client stores the JWT (typically in localStorage or memory)
3. Every subsequent request includes `Authorization: Bearer <jwt>`
4. Server verifies the signature and extracts the user's identity

**Our code implementation spans 3 files:**

| File | Purpose | Used When |
|------|---------|-----------|
| `JwtService.java` | Generate tokens, validate tokens, extract claims | Login (generate) + Every request (validate) |
| `JwtAuthenticationFilter.java` | Intercept requests, call JwtService, set SecurityContext | Every request automatically |
| `SecurityConfig.java` | Install the filter into the chain | Once at startup |

### Code Used on EVERY Request (Security Check)

Here's **exactly what code runs on every single authenticated request** in v2:

**Step 1: `JwtAuthenticationFilter.doFilterInternal()`** – runs on EVERY request

```java
// JwtAuthenticationFilter.java – THIS RUNS ON EVERY REQUEST

// 1. Get the Authorization header
final String authHeader = request.getHeader("Authorization");

// 2. No Bearer token? Skip → let other filters handle (might be a public endpoint)
if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    filterChain.doFilter(request, response);
    return;
}

// 3. Extract the JWT string
final String jwt = authHeader.substring(7);  // Remove "Bearer " prefix

try {
    // 4. Extract email from JWT (THIS ALSO VERIFIES THE SIGNATURE)
    final String userEmail = jwtService.extractUsername(jwt);
    
    // 5. Not already authenticated in this request?
    if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        
        // 6. Load user details from our store
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
        
        // 7. Is token valid (email matches + not expired)?
        if (jwtService.isTokenValid(jwt, userDetails)) {
            
            // 8. Set the user as authenticated for this request
            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }
} catch (Exception e) {
    // Invalid/expired/tampered token → user stays unauthenticated
}

// 9. Continue to next filter (authorization rules check)
filterChain.doFilter(request, response);
```

**Step 2: `JwtService.extractUsername()`** – the signature verification

```java
// JwtService.java – CALLED ON EVERY AUTHENTICATED REQUEST

public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);  // Gets "sub" = email
}

private <T> T extractClaim(String token, Function<Claims, T> resolver) {
    Claims claims = Jwts.parser()
        .verifyWith(getSigningKey())      // ← Use our secret key
        .build()
        .parseSignedClaims(token)          // ← VERIFY SIGNATURE HERE!
        .getPayload();                     // ← Only returns payload if signature is valid
    return resolver.apply(claims);
}

// The signing key comes from application.properties:
private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
}
```

**What `parseSignedClaims()` does internally:**
```
1. Split token: header.payload.signature
2. Decode header → get algorithm (HS256)
3. Recompute: HMAC-SHA256(header + "." + payload, ourSecretKey)
4. Compare computed signature with token's signature
5. Match? → Return claims (email, role, expiry)
   No match? → Throw SignatureException (token is forged/tampered)
```

**Step 3: `JwtService.isTokenValid()`** – additional checks

```java
// JwtService.java

public boolean isTokenValid(String token, UserDetails userDetails) {
    return extractUsername(token).equals(userDetails.getUsername())  // Email in token matches DB?
           && !isTokenExpired(token);                                // Not expired?
}

private boolean isTokenExpired(String token) {
    return extractClaim(token, Claims::getExpiration).before(new Date());
}
```

### What Happens with Different Attack Scenarios

| Attack | What happens | Code that prevents it |
|--------|-------------|----------------------|
| **Forged token** (attacker creates fake JWT) | `parseSignedClaims()` recomputes signature → doesn't match → `SignatureException` | `JwtService.extractClaim()` |
| **Tampered payload** (attacker changes email/role in existing token) | Signature no longer matches the modified payload → `SignatureException` | `JwtService.extractClaim()` |
| **Expired token** | `isTokenExpired()` checks `exp` claim against current time → returns false | `JwtService.isTokenValid()` |
| **Token for deleted user** | `loadUserByUsername()` throws `UsernameNotFoundException` | `JwtAuthenticationFilter` catch block |
| **No token sent** | Filter skips auth → user is unauthenticated → `authenticated()` rule returns 401 | `JwtAuthenticationFilter` early return |
| **Wrong role** | Token is valid but `hasRole("ADMIN")` check fails → 403 | `SecurityConfig` authorization rules |
| **Replay attack** (reusing a valid but old token) | Still works until `exp` – this is a known JWT limitation | Mitigated by short expiry time |

### Pros and Cons of JWT

| Pros ✅ | Cons ❌ |
|---------|--------|
| **Stateless** – server stores nothing, scales horizontally | **Can't revoke** – once issued, valid until expiry (no server-side logout) |
| **Self-contained** – carries user info, no DB lookup for session | **Size** – larger than a simple session ID (~300+ bytes vs ~32 bytes) |
| **Cross-service** – any service with the secret key can verify | **Payload is readable** – base64 encoded, not encrypted (don't put secrets in it) |
| **No CSRF vulnerability** – stored in JS memory, not in cookies | **Clock skew** – if server clocks differ, expiry checks can fail |
| **Performance** – signature verification is pure CPU (fast) | **Key rotation** – changing the secret invalidates ALL existing tokens |
| **Mobile-friendly** – no cookies needed | **Replay attacks** – stolen tokens work until they expire |
| **Standard** – RFC 7519, supported by all languages | **Refresh complexity** – need refresh token flow for long-lived sessions |

### When to Use JWT vs Other Approaches

| Approach | Best for | Our usage |
|----------|---------|-----------|
| **JWT Bearer (v2)** | APIs consumed by SPAs, mobile apps, microservices | When you need standard token-based auth |
| **HTTP Basic (v3)** | Simple APIs, quick prototyping, server-to-server | When simplicity matters most |
| **Session cookies** | Traditional web apps with server-rendered pages | Not used in this project |
| **OAuth 2.0 + JWT** | Third-party login (Google, GitHub), enterprise SSO | Overkill for this project |

### JWT vs HTTP Basic in Our Codebase – Security Comparison

| Security Aspect | JWT (v2) | HTTP Basic (v3) |
|----------------|----------|-----------------|
| **Password exposure** | Sent once (at login) | Sent with EVERY request (base64, not encrypted) |
| **Per-request cost** | Signature verification (CPU, ~0.1ms) | BCrypt hash (CPU, ~100ms) + DB lookup |
| **Token theft impact** | Attacker has access until token expires | Attacker has the actual password |
| **Logout** | Cannot truly logout (token still valid) | N/A (no session concept) |
| **HTTPS required?** | Highly recommended | **Absolutely essential** (password in every request) |
| **Code files involved** | `JwtService` + `JwtAuthenticationFilter` + `SecurityConfig` | Just `SecurityConfig` (uses Spring's built-in filter) |

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
