# Bookshop API

A simplified bookshop RESTful API built with Spring Boot for the Toptal screening round.

## Tech Stack

- **Java 17** + **Spring Boot 3.4.4**
- **Spring Security** + **JWT** (stateless authentication)
- **Spring Data JPA** + **H2** (embedded file-based database)
- **Jakarta Bean Validation** for input validation
- **Lombok** to reduce boilerplate
- **Maven** with Maven Wrapper

## Quick Start

```bash
cd bookshop
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/bookshopdb`, user: `sa`, no password)

## API Endpoints

### Authentication (Public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register with email + password |
| POST | `/api/auth/login` | Login, returns JWT token |

### Books (Public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/books` | List in-stock books (paginated) |
| GET | `/api/books?category=1,2` | Filter by categories |
| GET | `/api/books/{id}` | Get a single book |

### Categories (Public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/categories` | List all categories |
| GET | `/api/categories/{id}` | Get a single category |

### Cart (Authenticated)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/cart` | View cart |
| POST | `/api/cart/items/{bookId}` | Add book to cart |
| DELETE | `/api/cart/items/{bookId}` | Remove book from cart |
| POST | `/api/cart/checkout` | Purchase books in cart |

### Orders (Authenticated)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/orders` | View order history |

### Admin (Admin only)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/admin/categories` | Create category |
| PUT | `/api/admin/categories/{id}` | Update category |
| DELETE | `/api/admin/categories/{id}` | Delete category |
| POST | `/api/admin/books` | Create book (with stock) |
| PUT | `/api/admin/books/{id}` | Update book (stock not editable) |
| DELETE | `/api/admin/books/{id}` | Delete book |

## Authentication

All authenticated endpoints require a `Bearer` token in the `Authorization` header:
```
Authorization: Bearer <jwt_token>
```

## Permission Levels

1. **Anonymous**: Browse books, filter by category
2. **Authenticated User**: Add to cart, checkout, view orders
3. **Admin**: CRUD categories and books (admin role set via DB only)

## Key Design Decisions

- **Race Condition Handling**: Pessimistic locking (`SELECT FOR UPDATE`) during checkout prevents two users from buying the last copy of a book simultaneously.
- **Cart Expiry**: A scheduled task runs every 5 minutes to remove cart items older than 30 minutes.
- **Stock Immutability**: Stock is set at book creation and only decremented by checkout — no admin edit for stock.
- **Sold Out Books**: Books with 0 stock are automatically excluded from public listings.
- **Pagination**: Book listings support pagination for handling 10,000+ books efficiently.

## Making a User Admin

Connect to the H2 console and run:
```sql
UPDATE USERS SET ROLE = 'ADMIN' WHERE EMAIL = 'admin@example.com';
```
