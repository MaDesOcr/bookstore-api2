package com.example.bookstore.controller;

import com.example.bookstore.dto.*;
import com.example.bookstore.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    
    public BookController(BookService bookService) {
		super();
		this.bookService = bookService;
	}

	@GetMapping
    public List<BookDto> findAll() { return bookService.findAll(); }

    @GetMapping("/{id}")
    public BookDto findById(@PathVariable Long id) { return bookService.findById(id); }

    @GetMapping("/category/{category}")
    public List<BookDto> findByCategory(@PathVariable String category) {
        return bookService.findByCategory(category);
    }

    @GetMapping("/search")
    public List<BookDto> search(@RequestParam String q) { return bookService.search(q); }

    @PostMapping
    public ResponseEntity<BookDto> create(@Valid @RequestBody CreateBookRequest req) {
        BookDto created = bookService.createBook(req);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public BookDto update(@PathVariable Long id, @Valid @RequestBody UpdateBookRequest req) {
        return bookService.updateBook(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/purchase")
    public BookDto purchase(@PathVariable Long id, @RequestParam int quantity) {
        return bookService.purchase(id, quantity);
    }
}
