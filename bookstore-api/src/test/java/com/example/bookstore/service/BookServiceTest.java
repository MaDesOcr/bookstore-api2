package com.example.bookstore.service;

import com.example.bookstore.dto.*;
import com.example.bookstore.exception.*;
import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
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
            
        }

        @Test
        void findById_ShouldThrowException_WhenBookNotFound() {
           
        }
    }

    @Nested
    @DisplayName("Tests sur l'achat (Business Logic)")
    class PurchaseTests {

        @Test
        void purchase_ShouldDecreaseStock_WhenStockIsSufficient() {
           
        }

        @Test
        void purchase_ShouldThrowException_WhenStockIsInsufficient() {
           
        }
    }

    @Nested
    @DisplayName("Tests sur la suppression")
    class DeleteTests {

        @Test
        void deleteBook_ShouldCallDelete_WhenBookExists() {
          
        }

        @Test
        void deleteBook_ShouldThrowException_WhenBookDoesNotExist() {
           
        }
    }
}