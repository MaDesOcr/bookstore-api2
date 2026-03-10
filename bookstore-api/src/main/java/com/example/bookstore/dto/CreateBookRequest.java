package com.example.bookstore.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateBookRequest(
    @NotBlank String title,
    @NotBlank String author,
    String isbn,
    String category,
    @NotNull @DecimalMin("0.00") BigDecimal price,
    @Min(0) Integer stock
) {
    public CreateBookRequest(String title, String author, String category, BigDecimal price, Integer stock) {
        this(title, author, null, category, price, stock);
    }
}
