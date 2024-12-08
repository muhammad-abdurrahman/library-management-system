package com.github.muhammadabdurrahman.librarymanagementsystem.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.github.muhammadabdurrahman.librarymanagementsystem.business.exception.BookNotFoundException;
import com.github.muhammadabdurrahman.librarymanagementsystem.business.model.Book;
import com.github.muhammadabdurrahman.librarymanagementsystem.business.store.BookStore;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {

  @Mock
  private BookStore bookStore;

  @InjectMocks
  private LibraryService underTest;

  @Test
  void shouldAddBook() {
    // given
    var book = mock(Book.class);
    doNothing().when(bookStore).addBook(book);

    // when
    underTest.addBook(book);

    // then
    verify(bookStore).addBook(book);
  }

  @Test
  void shouldRemoveBook() {
    // given
    var isbn = "isbn";
    doNothing().when(bookStore).removeBook(isbn);

    // when
    underTest.removeBook(isbn);

    // then
    verify(bookStore).removeBook(isbn);
  }

  @Test
  void shouldFindBookByIsbnWhenBookExists() {
    // given
    var isbn = "isbn";
    var expected = mock(Book.class);
    doReturn(Optional.of(expected)).when(bookStore).findBookByIsbn(isbn);

    // when
    var actual = underTest.findBookByIsbn(isbn);

    // then
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void shouldThrowExceptionWhenBookNotFoundByIsbn() {
    // given
    var isbn = "isbn";
    doReturn(Optional.empty()).when(bookStore).findBookByIsbn(isbn);

    // when / then
    assertThatThrownBy(() -> underTest.findBookByIsbn(isbn))
        .isInstanceOf(BookNotFoundException.class)
        .hasMessageContaining("Book with ISBN %s not found".formatted(isbn));
  }

  @Test
  void shouldFindBooksByAuthor() {
    // given
    var author = "author";
    var expected = Arrays.asList(mock(Book.class), mock(Book.class));
    doReturn(expected).when(bookStore).findBooksByAuthor(author);

    // when
    var actual = underTest.findBooksByAuthor(author);

    // then
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void shouldBorrowBook() {
    // given
    var isbn = "isbn";
    doNothing().when(bookStore).borrowBook(isbn);

    // when
    underTest.borrowBook(isbn);

    // then
    verify(bookStore).borrowBook(isbn);
  }

  @Test
  void shouldReturnBook() {
    // given
    var isbn = "isbn";
    doNothing().when(bookStore).returnBook(isbn);

    // when
    underTest.returnBook(isbn);

    // then
    verify(bookStore).returnBook(isbn);
  }
}