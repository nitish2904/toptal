package com.toptal.bookshop;

import java.util.Locale;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toptal.bookshop.entity.Role;
import com.toptal.bookshop.entity.User;
import com.toptal.bookshop.repository.BookRepository;
import com.toptal.bookshop.repository.CartItemRepository;
import com.toptal.bookshop.repository.CategoryRepository;
import com.toptal.bookshop.repository.OrderRepository;
import com.toptal.bookshop.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookshopIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        cartItemRepository.deleteAll();
        orderRepository.deleteAll();
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Register a regular user
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"user@test.com\",\"password\":\"password123\"}"));

        MvcResult userLogin = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"user@test.com\",\"password\":\"password123\"}"))
                .andReturn();
        userToken = objectMapper.readTree(userLogin.getResponse().getContentAsString()).get("token").asText();

        // Create admin user directly in DB
        User admin = new User();
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        MvcResult adminLogin = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@test.com\",\"password\":\"admin123\"}"))
                .andReturn();
        adminToken = objectMapper.readTree(adminLogin.getResponse().getContentAsString()).get("token").asText();
    }

    // ==================== AUTH TESTS ====================

    @Test
    void registerUser_success() throws Exception {
        userRepository.deleteAll();
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"new@test.com\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void registerUser_duplicateEmail_returns409() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"user@test.com\",\"password\":\"password123\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void registerUser_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"notanemail\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginUser_success() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"user@test.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void loginUser_wrongPassword_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"user@test.com\",\"password\":\"wrongpassword\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== CATEGORY TESTS ====================

    @Test
    void adminCreateCategory_success() throws Exception {
        mockMvc.perform(post("/api/admin/categories")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Fiction\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Fiction"));
    }

    @Test
    void userCreateCategory_returns403() throws Exception {
        mockMvc.perform(post("/api/admin/categories")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Fiction\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousListCategories_success() throws Exception {
        mockMvc.perform(post("/api/admin/categories")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Science\"}"));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Science"));
    }

    @Test
    void adminUpdateCategory_success() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/categories")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Old Name\"}"))
                .andReturn();
        Long catId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/api/admin/categories/" + catId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"New Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @Test
    void adminDeleteCategory_success() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/categories")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"ToDelete\"}"))
                .andReturn();
        Long catId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/admin/categories/" + catId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    // ==================== BOOK TESTS ====================

    @Test
    void adminCreateBook_success() throws Exception {
        Long catId = createCategoryAsAdmin("Fiction");

        mockMvc.perform(post("/api/admin/books")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson("Test Book", "Author", 19.99, 10, 2024, catId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    void anonymousBrowseBooks_success() throws Exception {
        Long catId = createCategoryAsAdmin("Fiction");
        createBookAsAdmin("Book1", 5, catId);

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void soldOutBooks_hiddenFromListing() throws Exception {
        Long catId = createCategoryAsAdmin("Fiction");
        createBookAsAdmin("SoldOut", 0, catId);

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void filterBooksByCategory() throws Exception {
        Long cat1Id = createCategoryAsAdmin("Fiction");
        Long cat2Id = createCategoryAsAdmin("Science");

        createBookAsAdmin("FictionBook", 5, cat1Id);
        createBookAsAdmin("ScienceBook", 3, cat2Id);

        mockMvc.perform(get("/api/books").param("category", cat1Id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("FictionBook"));
    }

    // ==================== CART & CHECKOUT TESTS ====================

    @Test
    void addToCart_success() throws Exception {
        Long catId = createCategoryAsAdmin("Cat");
        Long bookId = createBookAsAdmin("CartBook", 10, catId);

        mockMvc.perform(post("/api/cart/items/" + bookId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void addToCart_unauthenticated_returns403() throws Exception {
        mockMvc.perform(post("/api/cart/items/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void removeFromCart_success() throws Exception {
        Long catId = createCategoryAsAdmin("Cat");
        Long bookId = createBookAsAdmin("RemoveBook", 10, catId);

        mockMvc.perform(post("/api/cart/items/" + bookId)
                .header("Authorization", "Bearer " + userToken));

        mockMvc.perform(delete("/api/cart/items/" + bookId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void checkout_success() throws Exception {
        Long catId = createCategoryAsAdmin("Cat");
        Long bookId = createBookAsAdmin("CheckoutBook", 5, catId);

        mockMvc.perform(post("/api/cart/items/" + bookId)
                .header("Authorization", "Bearer " + userToken));

        mockMvc.perform(post("/api/cart/checkout")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)));

        // Cart should be empty after checkout
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void checkout_emptyCart_returns400() throws Exception {
        mockMvc.perform(post("/api/cart/checkout")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkout_decrementsStock() throws Exception {
        Long catId = createCategoryAsAdmin("Cat");
        Long bookId = createBookAsAdmin("StockBook", 3, catId);

        mockMvc.perform(post("/api/cart/items/" + bookId)
                .header("Authorization", "Bearer " + userToken));

        mockMvc.perform(post("/api/cart/checkout")
                .header("Authorization", "Bearer " + userToken));

        // Check stock is now 2
        mockMvc.perform(get("/api/books/" + bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(2));
    }

    // ==================== ORDER TESTS ====================

    @Test
    void getOrders_success() throws Exception {
        Long catId = createCategoryAsAdmin("Cat");
        Long bookId = createBookAsAdmin("OrderBook", 10, catId);

        mockMvc.perform(post("/api/cart/items/" + bookId)
                .header("Authorization", "Bearer " + userToken));
        mockMvc.perform(post("/api/cart/checkout")
                .header("Authorization", "Bearer " + userToken));

        mockMvc.perform(get("/api/orders")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getOrders_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isForbidden());
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    void createBook_negativePrice_returns400() throws Exception {
        Long catId = createCategoryAsAdmin("Test");

        mockMvc.perform(post("/api/admin/books")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson("Bad", "A", -1.0, 5, 2024, catId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategory_blankName_returns400() throws Exception {
        mockMvc.perform(post("/api/admin/categories")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    // ==================== HELPERS ====================

    private Long createCategoryAsAdmin(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/categories")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\"}"))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long createBookAsAdmin(String title, int stock, Long categoryId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/books")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson(title, "Author", 9.99, stock, 2024, categoryId)))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private String bookJson(String title, String author, double price, int stock, int year, Long catId) {
        return String.format(Locale.US,
                "{\"title\":\"%s\",\"author\":\"%s\",\"price\":%.2f,\"stock\":%d,\"yearPublished\":%d,\"categoryId\":%d}",
                title, author, price, stock, year, catId);
    }
}
