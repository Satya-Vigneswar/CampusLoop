package com.campusloop.campusloop;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.campusloop.campusloop.entity.Product;
import com.campusloop.campusloop.config.MarketplaceSchemaMigration;
import com.campusloop.campusloop.repository.ProductRepository;
import com.campusloop.campusloop.repository.WishlistItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:campusloop-auth-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
class MarketplaceFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MarketplaceSchemaMigration schemaMigration;

    @BeforeEach
    void clearProducts() {
        wishlistItemRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void startupMigrationRemovesTheRedundantConditionColumn() throws Exception {
        jdbcTemplate.execute(
                "ALTER TABLE marketplace_products ADD COLUMN item_condition varchar(20) NOT NULL DEFAULT 'Good'");

        schemaMigration.run(new DefaultApplicationArguments());

        var columns = jdbcTemplate.queryForList(
                """
                SELECT COLUMN_NAME
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = 'MARKETPLACE_PRODUCTS'
                """,
                String.class);
        assertTrue(columns.contains("CONDITION"));
        assertFalse(columns.contains("ITEM_CONDITION"));
    }

    @Test
    void productsCanBeCreatedSearchedViewedAndDeletedByTheirOwner() throws Exception {
        MockHttpSession ownerSession = session(101L, "Taylor Student");
        MockHttpSession anotherSession = session(202L, "Jordan Student");

        mockMvc.perform(get("/products/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/").session(ownerSession))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(containsString("No listings just yet")));

        mockMvc.perform(post("/products")
                        .session(ownerSession)
                        .param("name", "Invalid listing")
                        .param("description", "A test product")
                        .param("price", "0")
                        .param("category", "Books")
                        .param("condition", "Good")
                        .param("imageUrl", "https://example.com/item.jpg"))
                .andExpect(status().isOk())
                .andExpect(view().name("product-form"))
                .andExpect(content().string(containsString("Price must be greater than zero.")));
        assertTrue(productRepository.findAll().isEmpty());

        mockMvc.perform(post("/products")
                        .session(ownerSession)
                        .param("name", "Organic Chemistry")
                        .param("description", "Second edition, in great shape.")
                        .param("price", "55.50")
                        .param("category", "Books")
                        .param("condition", "Good")
                        .param("imageUrl", "https://example.com/chemistry.jpg")
                        .param("sellerPhone", "+1 415 555 2671"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/products")
                        .session(ownerSession)
                        .param("name", "Scientific Calculator")
                        .param("description", "Works perfectly.")
                        .param("price", "20.00")
                        .param("category", "Stationery")
                        .param("condition", "Like New")
                        .param("imageUrl", "https://example.com/calculator.jpg"))
                .andExpect(status().is3xxRedirection());

        assertEquals(2, productRepository.count());
        Product chemistry = productRepository.findByNameContainingIgnoreCaseOrderByCreatedAtDesc("Chemistry")
                .get(0);
        Product calculator = productRepository.findByNameContainingIgnoreCaseOrderByCreatedAtDesc("Calculator")
                .get(0);

        mockMvc.perform(get("/").session(ownerSession)
                        .param("q", "calcul")
                        .param("category", "Stationery"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Scientific Calculator")))
                .andExpect(content().string(not(containsString("Organic Chemistry"))));

        mockMvc.perform(get("/products/" + chemistry.getId()).session(ownerSession))
                .andExpect(status().isOk())
                .andExpect(view().name("product-details"))
                .andExpect(content().string(containsString("Second edition, in great shape.")))
                .andExpect(content().string(containsString("Taylor Student")))
                .andExpect(content().string(containsString("Contact seller on WhatsApp")))
                .andExpect(content().string(containsString("https://wa.me/14155552671")));

        mockMvc.perform(post("/wishlist/" + chemistry.getId() + "/toggle").session(anotherSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        mockMvc.perform(get("/wishlist").session(anotherSession))
                .andExpect(status().isOk())
                .andExpect(view().name("wishlist"))
                .andExpect(content().string(containsString("Organic Chemistry")))
                .andExpect(content().string(not(containsString("Scientific Calculator"))));
        mockMvc.perform(get("/wishlist").session(ownerSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Your wishlist is ready for good finds")))
                .andExpect(content().string(not(containsString("Organic Chemistry"))));

        mockMvc.perform(post("/wishlist/" + chemistry.getId() + "/remove").session(anotherSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wishlist"));
        mockMvc.perform(get("/wishlist").session(anotherSession))
                .andExpect(content().string(not(containsString("Organic Chemistry"))));
        mockMvc.perform(post("/wishlist/" + chemistry.getId() + "/toggle").session(anotherSession))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/my-listings").session(ownerSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Organic Chemistry")))
                .andExpect(content().string(containsString("Scientific Calculator")));

        mockMvc.perform(post("/products/" + chemistry.getId() + "/delete").session(anotherSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-listings"));
        assertTrue(productRepository.existsById(chemistry.getId()));

        mockMvc.perform(post("/products/" + chemistry.getId() + "/delete").session(ownerSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-listings"));
        assertFalse(productRepository.existsById(chemistry.getId()));
        assertFalse(wishlistItemRepository.existsByStudentIdAndProduct_Id(202L, chemistry.getId()));
        assertTrue(productRepository.existsById(calculator.getId()));
        assertEquals(1, productRepository.count());
    }

    private MockHttpSession session(Long userId, String userName) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", userId);
        session.setAttribute("userName", userName);
        return session;
    }
}
