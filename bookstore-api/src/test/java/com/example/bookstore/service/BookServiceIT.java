package com.example.bookstore.service;

import com.example.bookstore.AbstractIntegrationTest;
import com.example.bookstore.dto.BookDto;
import com.example.bookstore.dto.CreateBookRequest;
import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.exception.InsufficientStockException;
import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests d'intégration de la couche service — TP 3.
 * Hérite de AbstractIntegrationTest (PostgreSQL Docker partagé).
 */
@DisplayName("Tests d'intégration — BookService")
class BookServiceIT extends AbstractIntegrationTest {

    @Autowired private BookService bookService;
    @Autowired private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    @Test
    @DisplayName("createBook() persiste en BDD et retourne un DTO avec ID")
    void createBook_persistsAndReturnsDto() {
        var req = new CreateBookRequest("Effective Java", "J. Bloch",
                                        "TECH", new BigDecimal("39.99"), 50);

        BookDto result = bookService.createBook(req);

        assertThat(result.id()).isNotNull();
        assertThat(result.title()).isEqualTo("Effective Java");

        // Vérifier la persistance réelle en BDD
        var persisted = bookRepository.findById(result.id()).orElseThrow();
        assertThat(persisted.getStock()).isEqualTo(50);
        assertThat(persisted.getCategory()).isEqualTo("TECH");
    }

    @Test
    @DisplayName("createBook() avec ISBN existant lève DuplicateIsbnException")
    void createBook_withExistingIsbn_throwsDuplicateIsbnException() {
        var req1 = new CreateBookRequest("Clean Code", "R. Martin",
                                         "978-0132350884", "TECH", new BigDecimal("29.99"), 10);
        bookService.createBook(req1);

        var req2 = new CreateBookRequest("Autre", "Auteur",
                                         "978-0132350884", "TECH", new BigDecimal("19.99"), 5);

        assertThatThrownBy(() -> bookService.createBook(req2))
            .isInstanceOf(com.example.bookstore.exception.DuplicateIsbnException.class);
    }

    @Test
    @DisplayName("purchase() décrémente le stock en BDD")
    void purchase_decreasesStockInDatabase() {
        Book book = bookRepository.save(
            new Book("Java Puzzlers", "J. Bloch", new BigDecimal("24.99"), 10));

        bookService.purchase(book.getId(), 3);

        Book updated = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(updated.getStock()).isEqualTo(7);
    }

    @Test
    @DisplayName("purchase() avec stock insuffisant lève InsufficientStockException")
    void purchase_withInsufficientStock_throwsException() {
        Book book = bookRepository.save(
            new Book("Book", "Author", new BigDecimal("10.00"), 2));

        assertThatThrownBy(() -> bookService.purchase(book.getId(), 10))
            .isInstanceOf(InsufficientStockException.class)
            .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("findById() avec ID inexistant lève BookNotFoundException")
    void findById_withNonExistentId_throwsBookNotFoundException() {
        assertThatThrownBy(() -> bookService.findById(999L))
            .isInstanceOf(BookNotFoundException.class)
            .hasMessageContaining("999");
    }
}
