package com.github.muhammadabdurrahman.librarymanagementsystem.business.exception;

public class InsufficientNumberOfAvailableCopiesException extends RuntimeException {

  public InsufficientNumberOfAvailableCopiesException(String isbn) {
    super("Insufficient number of available copies for book with ISBN %s".formatted(isbn));
  }
}
