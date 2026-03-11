package com.example.bookstore.controller;

import com.example.bookstore.dto.BookDto;
import com.example.bookstore.dto.CreateBookRequest;
import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.exception.DuplicateIsbnException;
import com.example.bookstore.exception.GlobalExceptionHandler;
import com.example.bookstore.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests UNITAIRES — POST /api/books (create)
 *
 * @WebMvcTest : charge UNIQUEMENT BookController + la couche Web Spring MVC.
 *   - Pas de BDD, pas de Service réel.
 *   - BookService est un mock Mockito (@MockBean).
 *
 * Ce que l'on teste ici :
 *   1. Le controller délègue bien au service
 *   2. Le code HTTP retourné est correct (201, 400, 409…)
 *   3. Le header Location est construit correctement
 *   4. La validation @Valid est déclenchée sur le body
 *   5. La réponse JSON est correctement sérialisée
 *
 * Ce que l'on NE teste PAS ici (ce serait un TI) :
 *   - Que le livre est réellement sauvegardé en BDD
 *   - Le comportement réel de BookService
 */
@WebMvcTest(controllers = {BookController.class, GlobalExceptionHandler.class})
@DisplayName("TU — POST /api/books (create)")
class BookControllerCreateTest {

    @Autowired
    private MockMvc mockMvc;         // Simule les requêtes HTTP sans serveur réel

    @Autowired
    private ObjectMapper objectMapper; // Sérialisation / désérialisation JSON

    @MockBean
    private BookService bookService;   // Le service est un mock — aucune BDD impliquée

    // ────────────────────────────────────────────────────────────────────────
    // CAS NOMINAL
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Cas nominal — requête valide")
    class NominalCases {

        @Test
        @DisplayName("retourne 201 Created quand le service réussit")
        void create_returns201_whenServiceSucceeds() throws Exception {
            // GIVEN — le service retourne un DTO avec l'ID généré
            BookDto serviceResponse = new BookDto(
                    42L, "Clean Code", "R. Martin", "978-0132350884",
                    new BigDecimal("29.99"), 10, "TECH", LocalDateTime.now());
            when(bookService.createBook(any(CreateBookRequest.class)))
                    .thenReturn(serviceResponse);

            CreateBookRequest requestBody = new CreateBookRequest(
                    "Clean Code", "R. Martin", "978-0132350884", "TECH",
                    new BigDecimal("29.99"), 10);

            // WHEN + THEN
            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isCreated());               // HTTP 201
        }

        @Test
        @DisplayName("retourne le livre créé dans le body JSON")
        void create_returnsCreatedBookInBody() throws Exception {
            // GIVEN
            BookDto serviceResponse = new BookDto(
                    42L, "Clean Code", "R. Martin", "978-0132350884",
                    new BigDecimal("29.99"), 10, "TECH", LocalDateTime.now());
            when(bookService.createBook(any())).thenReturn(serviceResponse);

            CreateBookRequest requestBody = new CreateBookRequest(
                    "Clean Code", "R. Martin", "978-0132350884", "TECH",
                    new BigDecimal("29.99"), 10);

            // WHEN + THEN — vérifier les champs du JSON retourné
            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(jsonPath("$.id",     is(42)))
                    .andExpect(jsonPath("$.title",  is("Clean Code")))
                    .andExpect(jsonPath("$.author", is("R. Martin")))
                    .andExpect(jsonPath("$.price",  is(29.99)))
                    .andExpect(jsonPath("$.stock",  is(10)));
        }

        @Test
        @DisplayName("retourne un header Location pointant vers le nouveau livre")
        void create_returnsLocationHeader_withNewBookUrl() throws Exception {
            // GIVEN — l'ID retourné par le service est 42
            BookDto serviceResponse = new BookDto(
                    42L, "Clean Code", "R. Martin", null,
                    new BigDecimal("29.99"), 10, "TECH", LocalDateTime.now());
            when(bookService.createBook(any())).thenReturn(serviceResponse);

            CreateBookRequest requestBody = new CreateBookRequest(
                    "Clean Code", "R. Martin", null, "TECH",
                    new BigDecimal("29.99"), 10);

            // WHEN + THEN — Location doit contenir /api/books/42
            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(header().string("Location", containsString("/api/books/42")));
        }

        @Test
        @DisplayName("délègue exactement une fois au service avec les bons paramètres")
        void create_delegatesToServiceExactlyOnce() throws Exception {
            // GIVEN
            BookDto serviceResponse = new BookDto(
                    1L, "Clean Code", "R. Martin", null,
                    new BigDecimal("29.99"), 5, "TECH", LocalDateTime.now());
            when(bookService.createBook(any())).thenReturn(serviceResponse);

            CreateBookRequest requestBody = new CreateBookRequest(
                    "Clean Code", "R. Martin", null, "TECH",
                    new BigDecimal("29.99"), 5);

            // WHEN
            mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)));

            // THEN — le service a été appelé exactement une fois
            verify(bookService, times(1)).createBook(any(CreateBookRequest.class));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // CAS D'ERREUR — Validation @Valid
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Validation @Valid — erreurs 400")
    class ValidationErrors {

        @Test
        @DisplayName("retourne 400 quand le titre est vide")
        void create_returns400_whenTitleIsBlank() throws Exception {
            // GIVEN — titre vide : viole @NotBlank sur CreateBookRequest.title
            String invalidBody = """
                    {
                      "title":  "",
                      "author": "R. Martin",
                      "price":  29.99,
                      "stock":  10
                    }
                    """;

            // WHEN + THEN — Spring MVC doit rejeter avant même d'appeler le service
            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isBadRequest());

            // Le service ne doit PAS avoir été appelé
            verifyNoInteractions(bookService);
        }

        @Test
        @DisplayName("retourne 400 quand l'auteur est absent")
        void create_returns400_whenAuthorIsNull() throws Exception {
            String invalidBody = """
                    {
                      "title": "Clean Code",
                      "price": 29.99
                    }
                    """;

            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(bookService);
        }

        @Test
        @DisplayName("retourne 400 quand le prix est absent")
        void create_returns400_whenPriceIsNull() throws Exception {
            String invalidBody = """
                    {
                      "title":  "Clean Code",
                      "author": "R. Martin",
                      "stock":  10
                    }
                    """;

            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(bookService);
        }

        @Test
        @DisplayName("retourne 400 quand le body est vide")
        void create_returns400_whenBodyIsEmpty() throws Exception {
            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(bookService);
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // CAS D'ERREUR — Exceptions métier remontées par le service
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Exceptions métier — erreurs 4xx")
    class BusinessErrors {

        @Test
        @DisplayName("retourne 409 quand le service lève DuplicateIsbnException")
        void create_returns409_whenServiceThrowsDuplicateIsbn() throws Exception {
            // GIVEN — le service signale un doublon d'ISBN
            when(bookService.createBook(any()))
                    .thenThrow(new DuplicateIsbnException("978-0132350884"));

            CreateBookRequest requestBody = new CreateBookRequest(
                    "Copie", "Auteur", "978-0132350884", "TECH",
                    new BigDecimal("9.99"), 1);

            // WHEN + THEN — GlobalExceptionHandler doit mapper en 409 Conflict
            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("retourne 415 si Content-Type n'est pas application/json")
        void create_returns415_whenContentTypeIsWrong() throws Exception {
            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("titre=Clean Code"))
                    .andExpect(status().isUnsupportedMediaType());

            verifyNoInteractions(bookService);
        }
    }
}