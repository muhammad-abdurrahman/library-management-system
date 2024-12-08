package com.github.muhammadabdurrahman.librarymanagementsystem.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.github.muhammadabdurrahman.librarymanagementsystem.business.model.Book;
import com.github.muhammadabdurrahman.librarymanagementsystem.business.service.LibraryService;
import com.github.muhammadabdurrahman.librarymanagementsystem.presentation.model.BookResource;
import com.github.muhammadabdurrahman.librarymanagementsystem.presentation.model.mapper.BookResourceMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

  @Mock
  private LibraryService libraryService;

  @Mock
  private BookResourceMapper mapper;

  @InjectMocks
  private BookController underTest;

  @Test
  void shouldFindBookByIsbn() {
    // given
    var isbn = "isbn";
    var book = Book.builder()
        .isbn(isbn)
        .title("title")
        .author("author")
        .publicationYear(2017)
        .availableCopies(5)
        .build();
    var expected = new BookResource(isbn, "title", "author", 2017, 5);
    doReturn(book).when(libraryService).findBookByIsbn(isbn);
    doReturn(expected).when(mapper).map(book);

    // when
    BookResource actual = underTest.findBookByIsbn(isbn);

    // then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  void shouldFindBooksByAuthor() {
    // given
    var author = "author";
    var book1 = Book.builder()
        .isbn("isbn1")
        .title("title1")
        .author(author)
        .publicationYear(2017)
        .availableCopies(5)
        .build();
    var book2 = Book.builder()
        .isbn("isbn2")
        .title("title2")
        .author(author)
        .publicationYear(2017)
        .availableCopies(3)
        .build();
    List<Book> books = List.of(book1, book2);
    var bookResource1 = new BookResource("isbn1", "title1", author, 2017, 5);
    var bookResource2 = new BookResource("isbn2", "title2", author, 2017, 3);
    List<BookResource> expected = List.of(bookResource1, bookResource2);
    doReturn(books).when(libraryService).findBooksByAuthor(author);
    doReturn(expected).when(mapper).map(books);

    // when
    List<BookResource> actual = underTest.findBooksByAuthor(author);

    // then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  void shouldAddBook() {
    // given
    var bookResource = new BookResource("isbn", "title", "author", 2017, 5);
    var book = Book.builder()
        .isbn("isbn")
        .title("title")
        .author("author")
        .publicationYear(2017)
        .availableCopies(5)
        .build();
    doReturn(book).when(mapper).map(bookResource);

    // when
    underTest.addBook(bookResource);

    // then
    verify(libraryService).addBook(book);
  }

  @Test
  void shouldBorrowBook() {
    // given
    var isbn = "isbn";

    // when
    underTest.borrowBook(isbn);

    // then
    verify(libraryService).borrowBook(isbn);
  }

  @Test
  void shouldReturnBook() {
    // given
    var isbn = "isbn";

    // when
    underTest.returnBook(isbn);

    // then
    verify(libraryService).returnBook(isbn);
  }

  @Test
  void shouldRemoveBook() {
    // given
    var isbn = "isbn";

    // when
    underTest.removeBook(isbn);

    // then
    verify(libraryService).removeBook(isbn);
  }
}