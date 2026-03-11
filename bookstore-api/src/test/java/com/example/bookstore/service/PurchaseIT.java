package com.example.bookstore.service;

import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PurchaseIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    // ─── TI-01 : Nominal ────────────────────────────────────────────────────

    @Test
    void purchase_withSufficientStock_shouldReturn200AndDecrementStock()
            throws Exception {
        // GIVEN
        
    }

    // ─── TI-02 : Limite – quantité = stock ─────────────────────────────────

    @Test
    void purchase_withQuantityEqualToStock_shouldSetStockToZero()
            throws Exception {
        
    }

    // ─── TI-03 : Erreur – stock insuffisant ────────────────────────────────

    @Test
    void purchase_withInsufficientStock_shouldReturn409AndLeaveStockUnchanged()
            throws Exception {
    }

    // ─── TI-04 : Erreur – livre inexistant ─────────────────────────────────

    @Test
    void purchase_withUnknownId_shouldReturn404()
            throws Exception {
        // GIVEN – aucun livre en base avec cet id

        // WHEN / THEN
        
    }

    // ─── TI-05 : Limite – quantité nulle ───────────────────────────────────

    @Test
    void purchase_withZeroQuantity_shouldReturn400()
            throws Exception {
        // GIVEN
       
    }

    // ─── TI-06 : Robustesse – achats successifs ────────────────────────────

    @Test
    void purchase_calledTwice_shouldDecrementStockCumulatively()
            throws Exception {
       
    }

    // ─── Helper ─────────────────────────────────────────────────────────────

    private Book bookWith(int stock) {
        Book b = new Book();
        b.setTitle("Test Book");
        b.setAuthor("Test Author");
        b.setIsbn("TEST-" + (int)(Math.random() * 99999));
        b.setPrice(BigDecimal.valueOf(9.99));
        b.setStock(stock);
        b.setCategory("TEST");
        return b;
    }
}