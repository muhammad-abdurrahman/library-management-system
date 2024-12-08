package com.github.muhammadabdurrahman.librarymanagementsystem.business.exception;

public class BookNotFoundException extends RuntimeException {

  public BookNotFoundException(String isbn) {
    super("Book with ISBN %s not found".formatted(isbn));
  }
}