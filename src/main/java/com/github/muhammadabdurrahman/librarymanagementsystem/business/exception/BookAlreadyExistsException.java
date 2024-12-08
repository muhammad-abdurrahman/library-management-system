package com.github.muhammadabdurrahman.librarymanagementsystem.business.exception;

public class BookAlreadyExistsException extends RuntimeException {

  public BookAlreadyExistsException(String isbn) {
    super("Book with ISBN %s already exists".formatted(isbn));
  }
}
