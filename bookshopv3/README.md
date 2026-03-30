# Bookshop API v3 – HTTP Basic Auth

A RESTful bookshop API built with **Spring Boot 3.4**, **Java 21**, and **in-memory HashMap storage**. 
Uses **HTTP Basic Authentication** instead of JWT tokens.

## Key Differences from v2
- **No JWT** – authentication uses HTTP Basic (email:password in every request)
- **No login endpoint** – after registering, use email/password as Basic Auth credentials
- **Simpler security** – no token management, no secret keys
- **Stateless** – each request is independently authenticated

## Tech Stack
- Java 21, Spring Boot 3.4, Spring Security (HTTP Basic)
- In-memory ConcurrentHashMap storage (no database)
- Swagger/OpenAPI 3.0

## Quick Start
```bash
cd bookshopv3
./mvnw spring-boot:run
```
API: http://localhost:8080  
Swagger: http://localhost:8080/swagger-ui.html

## Authentication

### Register (no auth needed)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"pass123"}'
```

### Use HTTP Basic for all other requests
```bash
# Browse books (no auth needed for GET)
curl http://localhost:8080/api/books

# Add to cart (requires Basic Auth)
curl -u user@test.com:pass123 -X POST http://localhost:8080/api/cart/items/1

# Admin operations (use pre-seeded admin)
curl -u admin@bookshop.com:admin123 -X POST http://localhost:8080/api/admin/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Fiction"}'
```

## Default Admin
- Email: `admin@bookshop.com`  
- Password: `admin123`

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /api/auth/register | None | Register new user |
| GET | /api/books | None | List in-stock books (paginated, filterable) |
| GET | /api/books/{id} | None | Get book details |
| GET | /api/categories | None | List categories |
| GET | /api/categories/{id} | None | Get category |
| POST | /api/admin/categories | ADMIN | Create category |
| PUT | /api/admin/categories/{id} | ADMIN | Update category |
| DELETE | /api/admin/categories/{id} | ADMIN | Delete category |
| POST | /api/admin/books | ADMIN | Create book |
| PUT | /api/admin/books/{id} | ADMIN | Update book |
| DELETE | /api/admin/books/{id} | ADMIN | Delete book |
| GET | /api/cart | USER | View cart |
| POST | /api/cart/items/{bookId} | USER | Add to cart |
| DELETE | /api/cart/items/{bookId} | USER | Remove from cart |
| POST | /api/cart/checkout | USER | Checkout |
| GET | /api/orders | USER | View orders |

## Business Rules
1. Stock is set at creation and immutable via update API
2. Sold-out books (stock=0) are hidden from listings
3. One copy per book per user in cart
4. Checkout is synchronized to prevent race conditions
5. Cart items expire after 30 minutes
