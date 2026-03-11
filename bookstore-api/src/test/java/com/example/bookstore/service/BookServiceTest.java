package com.example.bookstore.service;

import com.example.bookstore.dto.*;
import com.example.bookstore.exception.*;
import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book sampleBook;

    @BeforeEach
    void setUp() {
        sampleBook = new Book();
        sampleBook.setId(1L);
        sampleBook.setTitle("Le Petit Prince");
        sampleBook.setAuthor("Saint-Exupéry");
        sampleBook.setStock(10);
        sampleBook.setIsbn("123456789");
    }

    @Nested
    @DisplayName("Tests sur la récupération (Read)")
    class RetrievalTests {

        @Test
        void findById_ShouldReturnDto_WhenBookExists() {
            when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
            BookDto b = bookService.findById(1L);
            assertNotNull(b);
            assertEquals(b.title(), sampleBook.getTitle());
            assertInstanceOf(BookDto.class, b);
        }

        @Test
        void findById_ShouldThrowException_WhenBookNotFound() {
        	when(bookRepository.findById(any())).thenReturn(Optional.empty());
            assertThrows(BookNotFoundException.class, ()->bookService.findById(1L));
        }
    }

    @Nested
    @DisplayName("Tests sur l'achat (Business Logic)")
    class PurchaseTests {

        @Test
        void purchase_ShouldDecreaseStock_WhenStockIsSufficient() {
            when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
            when(bookRepository.save(any())).thenReturn(sampleBook);
            bookService.purchase(1L, 1);
            assertEquals(9, sampleBook.getStock());
        }

        @Test
        void purchase_ShouldThrowException_WhenStockIsInsufficient() {
           //InsufficientStockException
           when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
           InsufficientStockException e = assertThrows(InsufficientStockException.class, ()->bookService.purchase(1L, 100));
           assertEquals("Insufficient stock for book " + 1L + ". Requested: " + 100 + ", Available: " + 10 +"" , e.getMessage());
        }
    }

    @Nested
    @DisplayName("Tests sur la suppression")
    class DeleteTests {
    	
    	@Disabled
        @Test
        void deleteBook_ShouldCallDelete_WhenBookExists() {
        	bookRepository.save(sampleBook);
        	assertNotNull(bookService.findById(1L));
        	bookService.deleteBook(1L);
            assertThrows(BookNotFoundException.class, ()->bookService.findById(1L));
        }

        @Test
        void deleteBook_ShouldThrowException_WhenBookDoesNotExist() {
        	when(bookRepository.existsById(any())).thenReturn(false);
            assertThrows(BookNotFoundException.class, ()->bookService.deleteBook(99L));
        }
    }
}