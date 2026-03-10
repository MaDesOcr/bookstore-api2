package com.example.bookstore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookDto(Long id, String title, String author, String isbn,
                      BigDecimal price, Integer stock, String category,
                      LocalDateTime createdAt) {}
