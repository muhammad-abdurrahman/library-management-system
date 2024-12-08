package com.github.muhammadabdurrahman.librarymanagementsystem.business.store;

import com.github.muhammadabdurrahman.librarymanagementsystem.business.model.Book;
import java.util.List;
import java.util.Optional;

public interface BookStore {

  void addBook(Book book);

  void removeBook(String isbn);

  Optional<Book> findBookByIsbn(String isbn);

  List<Book> findBooksByAuthor(String author);

  void borrowBook(String isbn);

  void returnBook(String isbn);

}
