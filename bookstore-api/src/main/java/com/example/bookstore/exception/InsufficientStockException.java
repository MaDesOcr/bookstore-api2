package com.example.bookstore.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(Long bookId, int requested, int available) {
        super("Insufficient stock for book " + bookId +
              ". Requested: " + requested + ", Available: " + available);
    }
}
