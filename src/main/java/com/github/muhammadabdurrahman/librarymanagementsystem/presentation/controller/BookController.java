package com.github.muhammadabdurrahman.librarymanagementsystem.presentation.controller;

import com.github.muhammadabdurrahman.librarymanagementsystem.business.service.LibraryService;
import com.github.muhammadabdurrahman.librarymanagementsystem.presentation.model.BookResource;
import com.github.muhammadabdurrahman.librarymanagementsystem.presentation.model.mapper.BookResourceMapper;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BookController implements BookApi {

  private final LibraryService libraryService;
  private final BookResourceMapper mapper;

  @Override
  public BookResource findBookByIsbn(String isbn) {
    return mapper.map(libraryService.findBookByIsbn(isbn));
  }

  @Override
  public List<BookResource> findBooksByAuthor(String author) {
    return mapper.map(libraryService.findBooksByAuthor(author));
  }

  @Override
  public void addBook(@Valid BookResource book) {
    libraryService.addBook(mapper.map(book));
  }

  @Override
  public void borrowBook(String isbn) {
    libraryService.borrowBook(isbn);
  }

  @Override
  public void returnBook(String isbn) {
    libraryService.returnBook(isbn);
  }

  @Override
  public void removeBook(String isbn) {
    libraryService.removeBook(isbn);
  }
}
