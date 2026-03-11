package com.example.bookstore.service;

import com.example.bookstore.dto.BookDto;
import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests d'intégration — BookService.findById()
 *
 * La méthode dans BookService :
 * ─────────────────────────────
 *   public BookDto findById(Long id) {
 *       return toDto(bookRepository.findById(id)
 *           .orElseThrow(() -> new BookNotFoundException(id)));
 *   }
 *
 * Ce que l'on teste ici (et qu'un TU ne peut pas garantir) :
 * ───────────────────────────────────────────────────────────
 *   1. Le SELECT est bien exécuté en BDD
 *   2. Tous les champs sont correctement mappés (titre, auteur, prix, stock, isbn, category)
 *   3. createdAt est renseigné (rempli par @PrePersist au moment du save)
 *   4. BookNotFoundException est levée avec le bon ID quand le livre est absent
 *   5. Deux livres en BDD — on récupère bien le bon via son ID
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("TI — BookService.findById()")
class BookServiceFindByIdIT {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    // ────────────────────────────────────────────────────────────────────────
    // CAS 1 — Livre trouvé : le DTO retourné correspond à ce qui est en BDD
    //
    // On insère directement via le repository (pas via le service)
    // pour tester findById() en isolation.
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("retourne un DTO dont tous les champs correspondent à la BDD")
    void findById_returnsDto_withAllFieldsMappedCorrectly() {
        // GIVEN — insérer un livre complet directement en BDD
        Book book = new Book("Clean Code", "R. Martin", new BigDecimal("29.99"));
        book.setIsbn("978-0132350884");
        book.setCategory("TECH");
        book.setStock(42);
        Book saved = bookRepository.save(book);

        // WHEN
        BookDto result = bookService.findById(saved.getId());

        // THEN — chaque champ du DTO doit correspondre à ce qui est en BDD
        assertThat(result.id()).isEqualTo(saved.getId());
        assertThat(result.title()).isEqualTo("Clean Code");
        assertThat(result.author()).isEqualTo("R. Martin");
        assertThat(result.isbn()).isEqualTo("978-0132350884");
        assertThat(result.price()).isEqualByComparingTo("29.99");
        assertThat(result.stock()).isEqualTo(42);
        assertThat(result.category()).isEqualTo("TECH");
    }

    // ────────────────────────────────────────────────────────────────────────
    // CAS 2 — createdAt est renseigné
    //
    // @PrePersist remplit createdAt au moment du flush JPA.
    // findById() doit retourner cette valeur via le mapping toDto().
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("retourne un createdAt non null (rempli par @PrePersist)")
    void findById_returnsDto_withCreatedAtSet() {
        // GIVEN
        Book saved = bookRepository.save(
                new Book("Effective Java", "J. Bloch", new BigDecimal("44.99")));

        // WHEN
        BookDto result = bookService.findById(saved.getId());

        // THEN
        assertThat(result.createdAt()).isNotNull();
    }

    // ────────────────────────────────────────────────────────────────────────
    // CAS 3 — Champs optionnels null : isbn et category peuvent être absents
    //
    // Un livre peut être créé sans ISBN ni catégorie.
    // Le DTO doit refléter ces null sans erreur de mapping.
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("retourne null pour isbn et category quand ils ne sont pas renseignés")
    void findById_returnsDto_withNullIsbnAndCategory_whenNotSet() {
        // GIVEN — livre sans isbn ni category
    	Book book = new Book("Clean Code", "R. Martin", new BigDecimal("29.99"));
        //book.setIsbn("978-0132350884");
        //book.setCategory("TECH");
        Book saved = bookRepository.save(book);

        // WHEN
        BookDto result = bookService.findById(saved.getId());

        // THEN
        assertThat(result.isbn()).isNull();
        assertThat(result.category()).isNull();

    }

    // ────────────────────────────────────────────────────────────────────────
    // CAS 4 — Deux livres en BDD : on retrouve le bon via l'ID
    //
    // Vérifie que le SELECT utilise bien l'ID comme critère de filtre
    // et ne retourne pas un mauvais livre.
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("retourne le bon livre quand plusieurs livres sont en BDD")
    void findById_returnsCorrectBook_whenMultipleBooksExist() {
        // GIVEN — deux livres en BDD avec des IDs différents
    	Book book1 = new Book("Book1", "R. Martin", new BigDecimal("29.99"));
    	Book book2 = new Book("Book2", "R. Martin", new BigDecimal("29.99"));
    	bookRepository.save(book1);
    	Book bToSearch = bookRepository.save(book2);
    	
        // WHEN — on cherche explicitement le deuxième
        BookDto result = bookService.findById(bToSearch.getId());

        // THEN — on doit obtenir book2, pas book1
        assertEquals("Book2", result.title());
    }

    // ────────────────────────────────────────────────────────────────────────
    // CAS 5 — Livre inexistant : BookNotFoundException levée avec le bon ID
    //
    // La méthode utilise orElseThrow(() -> new BookNotFoundException(id)).
    // On vérifie que l'exception contient l'ID qui n'existe pas.
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("lève BookNotFoundException quand l'ID n'existe pas en BDD")
    void findById_throwsBookNotFoundException_whenIdNotFound() {
        // GIVEN — BDD vide (setUp() a fait deleteAll)
    	
        // WHEN + THEN
    	assertThrows(BookNotFoundException.class, ()->bookService.findById(1L));
    	
    }

    // ────────────────────────────────────────────────────────────────────────
    // CAS 6 — Après suppression, le livre n'est plus trouvable
    //
    // Scénario enchaîné : save → findById OK → delete → findById lève exception.
    // Vérifie la cohérence de la BDD entre les opérations.
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("lève BookNotFoundException après suppression du livre")
    void findById_throwsException_afterBookIsDeleted() {
        // GIVEN — un livre existe puis est supprimé
    	Book book = new Book("Book1", "R. Martin", new BigDecimal("29.99"));
        Book saved = bookRepository.save(book);

        // Vérifier qu'il existe bien avant suppression
        BookDto result = bookService.findById(saved.getId());
        assertNotNull(result);
        // Supprimer le livre
        bookService.deleteBook(saved.getId());
        // WHEN + THEN — il ne doit plus être trouvable
    	assertThrows(BookNotFoundException.class, ()->bookService.findById(saved.getId()));

    }
}