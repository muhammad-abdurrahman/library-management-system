package com.github.muhammadabdurrahman.librarymanagementsystem.persistence.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.github.muhammadabdurrahman.librarymanagementsystem.business.exception.BookAlreadyExistsException;
import com.github.muhammadabdurrahman.librarymanagementsystem.business.exception.BookNotFoundException;
import com.github.muhammadabdurrahman.librarymanagementsystem.business.exception.InsufficientNumberOfAvailableCopiesException;
import com.github.muhammadabdurrahman.librarymanagementsystem.business.model.Book;
import com.github.muhammadabdurrahman.librarymanagementsystem.persistence.cache.BookCache;
import com.github.muhammadabdurrahman.librarymanagementsystem.persistence.model.BookEntity;
import com.github.muhammadabdurrahman.librarymanagementsystem.persistence.model.mapper.BookEntityMapper;
import com.github.muhammadabdurrahman.librarymanagementsystem.persistence.repository.BookJpaRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookStoreImplTest {

  @Mock
  private BookJpaRepository repository;

  @Mock
  private BookCache cache;

  @Mock
  private BookEntityMapper mapper;

  @InjectMocks
  private BookStoreImpl underTest;

  private Book book;
  private BookEntity bookEntity;

  @BeforeEach
  void setUp() {
    book = Book.builder()
        .isbn("isbn")
        .title("title")
        .author("author")
        .publicationYear(2017)
        .availableCopies(5)
        .build();
    bookEntity = new BookEntity("isbn", "title", "author", 2017, 5);
  }

  @Test
  void shouldAddBook() {
    // given
    doReturn(false).when(repository).existsById(book.getIsbn());
    doReturn(bookEntity).when(mapper).map(book);
    doReturn(bookEntity).when(repository).saveAndFlush(bookEntity);
    doNothing().when(cache).put(book.getIsbn(), book);

    // when
    underTest.addBook(book);

    // then
    verify(repository).saveAndFlush(bookEntity);
    verify(cache).put(book.getIsbn(), book);
  }

  @Test
  void shouldThrowExceptionWhenAddingExistingBook() {
    // given
    doReturn(true).when(repository).existsById(book.getIsbn());

    // when / then
    assertThatThrownBy(() -> underTest.addBook(book))
        .isInstanceOf(BookAlreadyExistsException.class)
        .hasMessageContaining("Book with ISBN %s already exists".formatted(book.getIsbn()));

    verify(repository, never()).saveAndFlush(any());
    verify(cache, never()).put(any(), any());
  }

  @Test
  void shouldRemoveBook() {
    // given
    doReturn(true).when(repository).existsById(book.getIsbn());
    doNothing().when(repository).deleteById(book.getIsbn());
    doNothing().when(repository).flush();
    doNothing().when(cache).remove(book.getIsbn());

    // when
    underTest.removeBook(book.getIsbn());

    // then
    verify(repository).deleteById(book.getIsbn());
    verify(repository).flush();
    verify(cache).remove(book.getIsbn());
  }

  @Test
  void shouldThrowExceptionWhenRemovingNonExistingBook() {
    // given
    doReturn(false).when(repository).existsById(book.getIsbn());

    // when / then
    var isbn = book.getIsbn();
    assertThatThrownBy(() -> underTest.removeBook(isbn))
        .isInstanceOf(BookNotFoundException.class)
        .hasMessageContaining("Book with ISBN %s not found".formatted(book.getIsbn()));

    verify(repository, never()).deleteById(any());
    verify(repository, never()).flush();
    verify(cache, never()).remove(any());
  }

  @Test
  void shouldFindBookByIsbn() {
    // given
    doReturn(Optional.empty()).when(cache).get(book.getIsbn());
    doReturn(book).when(mapper).map(bookEntity);
    doReturn(Optional.of(bookEntity)).when(repository).findById(book.getIsbn());
    doNothing().when(cache).put(book.getIsbn(), book);

    // when
    Optional<Book> actual = underTest.findBookByIsbn(book.getIsbn());

    // then
    assertThat(actual).isPresent().contains(book);
    verify(cache).put(book.getIsbn(), book);
  }

  @Test
  void shouldReturnEmptyWhenBookNotFoundByIsbn() {
    // given
    doReturn(Optional.empty()).when(cache).get(book.getIsbn());
    doReturn(Optional.empty()).when(repository).findById(book.getIsbn());

    // when
    Optional<Book> actual = underTest.findBookByIsbn(book.getIsbn());

    // then
    assertThat(actual).isNotPresent();
    verify(cache, never()).put(any(), any());
  }

  @Test
  void shouldBorrowBook() {
    // given
    doReturn(Optional.of(bookEntity)).when(repository).findByIdForUpdate(book.getIsbn());
    bookEntity.setAvailableCopies(1);

    // when
    underTest.borrowBook(book.getIsbn());

    // then
    assertThat(bookEntity.getAvailableCopies()).isZero();
    verify(repository).save(bookEntity);
    verify(cache).remove(book.getIsbn());
  }

  @Test
  void shouldThrowExceptionWhenBorrowingBookWithNoAvailableCopies() {
    // given
    doReturn(Optional.of(bookEntity)).when(repository).findByIdForUpdate(book.getIsbn());
    bookEntity.setAvailableCopies(0);

    // when / then
    var isbn = book.getIsbn();
    assertThatThrownBy(() -> underTest.borrowBook(isbn))
        .isInstanceOf(InsufficientNumberOfAvailableCopiesException.class)
        .hasMessageContaining("Insufficient number of available copies for book with ISBN %s".formatted(book.getIsbn()));

    verify(repository, never()).save(any());
    verify(cache, never()).remove(any());
  }

  @Test
  void shouldThrowExceptionWhenBorrowingBookNotFound() {
    // given
    doReturn(Optional.empty()).when(repository).findByIdForUpdate(book.getIsbn());

    // when / then
    var isbn = book.getIsbn();
    assertThatThrownBy(() -> underTest.borrowBook(isbn))
        .isInstanceOf(BookNotFoundException.class)
        .hasMessageContaining("Book with ISBN %s not found".formatted(book.getIsbn()));

    verify(repository, never()).save(any());
    verify(cache, never()).remove(any());
  }

  @Test
  void shouldReturnBook() {
    // given
    doReturn(Optional.of(bookEntity)).when(repository).findByIdForUpdate(book.getIsbn());
    bookEntity.setAvailableCopies(0);

    // when
    underTest.returnBook(book.getIsbn());

    // then
    assertThat(bookEntity.getAvailableCopies()).isEqualTo(1);
    verify(repository).save(bookEntity);
    verify(cache).remove(book.getIsbn());
  }

  @Test
  void shouldThrowExceptionWhenReturningBookNotFound() {
    // given
    doReturn(Optional.empty()).when(repository).findByIdForUpdate(book.getIsbn());

    // when / then
    var isbn = book.getIsbn();
    assertThatThrownBy(() -> underTest.returnBook(isbn))
        .isInstanceOf(BookNotFoundException.class)
        .hasMessageContaining("Book with ISBN %s not found".formatted(book.getIsbn()));

    verify(repository, never()).save(any());
    verify(cache, never()).remove(any());
  }

}