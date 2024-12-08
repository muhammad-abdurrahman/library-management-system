package com.github.muhammadabdurrahman.librarymanagementsystem.business.service;

import com.github.muhammadabdurrahman.librarymanagementsystem.business.exception.BookNotFoundException;
import com.github.muhammadabdurrahman.librarymanagementsystem.business.model.Book;
import com.github.muhammadabdurrahman.librarymanagementsystem.business.store.BookStore;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LibraryService {

  private final BookStore bookStore;

  public void addBook(Book book) {
    bookStore.addBook(book);
  }

  public void removeBook(String isbn) {
    bookStore.removeBook(isbn);
  }

  public Book findBookByIsbn(String isbn) {
    return bookStore.findBookByIsbn(isbn)
        .orElseThrow(() -> new BookNotFoundException(isbn));
  }

  public List<Book> findBooksByAuthor(String author) {
    return bookStore.findBooksByAuthor(author);
  }

  public void borrowBook(String isbn) {
    bookStore.borrowBook(isbn);
  }

  public void returnBook(String isbn) {
    bookStore.returnBook(isbn);
  }
}
