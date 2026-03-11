package com.example.bookstore.service;

import com.example.bookstore.dto.*;
import com.example.bookstore.exception.*;
import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
		super();
		this.bookRepository = bookRepository;
	}

	public List<BookDto> findAll() {
        return bookRepository.findAll().stream().map(this::toDto).toList();
    }

    public BookDto findById(Long id) {
        return toDto(bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id)));
    }

    public List<BookDto> findByCategory(String category) {
        return bookRepository.findByCategory(category).stream().map(this::toDto).toList();
    }

    public List<BookDto> search(String keyword) {
        return bookRepository.findByTitleContainingIgnoreCase(keyword).stream().map(this::toDto).toList();
    }

    @Transactional
    public BookDto createBook(CreateBookRequest req) {
        if (req.isbn() != null && bookRepository.existsByIsbn(req.isbn())) {
            throw new DuplicateIsbnException(req.isbn());
        }
        Book book = new Book();
            
        return toDto(bookRepository.save(book));
    }

    @Transactional
    public BookDto updateBook(Long id, UpdateBookRequest req) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));
        if (req.title()    != null) book.setTitle(req.title());
        if (req.author()   != null) book.setAuthor(req.author());
        if (req.price()    != null) book.setPrice(req.price());
        if (req.stock()    != null) book.setStock(req.stock());
        if (req.category() != null) book.setCategory(req.category());
        return toDto(bookRepository.save(book));
    }

    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) throw new BookNotFoundException(id);
        bookRepository.deleteById(id);
    }

    @Transactional
    public BookDto purchase(Long id, int quantity) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));
        if (book.getStock() < quantity) {
            throw new InsufficientStockException(id, quantity, book.getStock());
        }
        book.setStock(book.getStock() - quantity);
        return toDto(bookRepository.save(book));
    }

    private BookDto toDto(Book b) {
        return new BookDto(b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(),
                           b.getPrice(), b.getStock(), b.getCategory(), b.getCreatedAt());
    }
}
