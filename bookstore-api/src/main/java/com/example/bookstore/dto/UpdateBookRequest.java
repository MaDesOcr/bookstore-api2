package com.example.bookstore.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record UpdateBookRequest(
    String title,
    String author,
    @DecimalMin("0.00") BigDecimal price,
    @Min(0) Integer stock,
    String category
) {}
