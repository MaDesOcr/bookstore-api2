package com.example.bookstore.repository;

import com.example.bookstore.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

/**
 * Tests d'intégration du repository — TP 1 et TP 2.
 *
 * @DataJpaTest : charge uniquement la couche JPA (pas le contexte HTTP complet)
 * @Testcontainers : active le lifecycle JUnit 5 pour les containers
 * Replace.NONE : OBLIGATOIRE — sans ça, Spring substitue H2 en mémoire
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = NONE)
@DisplayName("Tests d'intégration — BookRepository")
class BookRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("bookstore_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    // ── TP 1 : Tests fondamentaux ─────────────────────────────────────────

    @Test
    @DisplayName("save() doit persister le livre et générer un ID")
    void save_persistsBookAndGeneratesId() {
        Book book = new Book("Clean Code", "R. Martin", new BigDecimal("29.99"));

        Book saved = bookRepository.save(book);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Clean Code");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("findAll() retourne tous les livres sauvegardés")
    void findAll_returnsAllBooks() {
        bookRepository.saveAll(List.of(
            new Book("Book A", "Author 1", new BigDecimal("10.00")),
            new Book("Book B", "Author 2", new BigDecimal("20.00")),
            new Book("Book C", "Author 3", new BigDecimal("30.00"))
        ));

        List<Book> books = bookRepository.findAll();

        assertThat(books).hasSize(3);
    }

    // ── TP 2 : Requêtes JPQL et contraintes ──────────────────────────────

    @Test
    @DisplayName("findByAuthor() retourne uniquement les livres du bon auteur")
    void findByAuthor_returnsOnlyMatchingBooks() {
        bookRepository.saveAll(List.of(
            new Book("Clean Code",    "R. Martin", new BigDecimal("29.99")),
            new Book("Clean Arch.",   "R. Martin", new BigDecimal("34.99")),
            new Book("DDD Distilled", "V. Vernon", new BigDecimal("39.99"))
        ));

        List<Book> result = bookRepository.findByAuthor("R. Martin");

        assertThat(result).hasSize(2)
            .extracting(Book::getTitle)
            .containsExactlyInAnyOrder("Clean Code", "Clean Arch.");
    }

    @Test
    @DisplayName("findByTitleContainingIgnoreCase() retourne les résultats en insensible à la casse")
    void findByTitleContaining_isCaseInsensitive() {
        bookRepository.save(new Book("Clean Code", "R. Martin", new BigDecimal("29.99")));
        bookRepository.save(new Book("Design Patterns", "GoF", new BigDecimal("54.99")));

        List<Book> result = bookRepository.findByTitleContainingIgnoreCase("clean");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("save() avec ISBN en double lève DataIntegrityViolationException")
    void save_withDuplicateIsbn_throwsException() {
        Book first = Book.builder()
            .title("Clean Code").author("R. Martin")
            .isbn("978-0132350884").price(new BigDecimal("29.99")).stock(10).build();
        bookRepository.saveAndFlush(first);

        Book duplicate = Book.builder()
            .title("Autre titre").author("Autre auteur")
            .isbn("978-0132350884").price(new BigDecimal("19.99")).stock(5).build();

        assertThatThrownBy(() -> bookRepository.saveAndFlush(duplicate))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("countGroupedByCategory() retourne les comptes par catégorie")
    void countGroupedByCategory_returnsCorrectCounts() {
        bookRepository.saveAll(List.of(
            Book.builder().title("T1").author("A").price(BigDecimal.TEN).stock(1).category("TECH").build(),
            Book.builder().title("T2").author("A").price(BigDecimal.TEN).stock(1).category("TECH").build(),
            Book.builder().title("F1").author("B").price(BigDecimal.TEN).stock(1).category("FICTION").build()
        ));

        List<Object[]> result = bookRepository.countGroupedByCategory();

        assertThat(result).hasSize(2);
    }
}
