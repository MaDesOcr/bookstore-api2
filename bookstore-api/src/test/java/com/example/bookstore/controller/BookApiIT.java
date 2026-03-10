package com.example.bookstore.controller;

import com.example.bookstore.AbstractIntegrationTest;
import com.example.bookstore.dto.BookDto;
import com.example.bookstore.dto.CreateBookRequest;
import com.example.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration HTTP bout-en-bout — TP 4.
 * Teste l'API REST complète : HTTP -> Controller -> Service -> BDD réelle.
 */
@DisplayName("Tests d'intégration HTTP — BookApi")
class BookApiIT extends AbstractIntegrationTest {

    @Autowired private TestRestTemplate http;
    @Autowired private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/books retourne 201 avec Location header")
    void createBook_returns201WithLocation() {
        var req = Map.of("title","Clean Code","author","R. Martin",
                         "price","29.99","stock",10);

        ResponseEntity<BookDto> resp = http.postForEntity("/api/books", req, BookDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getHeaders().getLocation()).isNotNull();
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().title()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("GET /api/books/{id} retourne 404 pour un ID inexistant")
    void getBook_nonExistentId_returns404() {
        ResponseEntity<String> resp = http.getForEntity("/api/books/999", String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("DELETE /api/books/{id} retourne 204, puis GET retourne 404")
    void deleteBook_returns204_thenGetReturns404() {
        var req = Map.of("title","ToDelete","author","Author","price","9.99","stock",1);
        var created = http.postForEntity("/api/books", req, BookDto.class);
        long id = created.getBody().id();

        http.delete("/api/books/" + id);

        ResponseEntity<String> gone = http.getForEntity("/api/books/" + id, String.class);
        assertThat(gone.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Scénario CRUD complet bout-en-bout")
    void fullCRUD_completeScenario() {
        // CREATE
        var body = Map.of("title","DDD","author","E. Evans","price","44.99","stock",20);
        var created = http.postForEntity("/api/books", body, BookDto.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long id = created.getBody().id();

        // READ
        var found = http.getForEntity("/api/books/" + id, BookDto.class);
        assertThat(found.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(found.getBody().title()).isEqualTo("DDD");

        // LIST
        var all = http.getForEntity("/api/books", BookDto[].class);
        assertThat(all.getBody()).hasSize(1);

        // DELETE
        http.delete("/api/books/" + id);
        var gone = http.getForEntity("/api/books/" + id, String.class);
        assertThat(gone.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
