package com.github.muhammadabdurrahman.librarymanagementsystem.persistence.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.muhammadabdurrahman.librarymanagementsystem.business.model.Book;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookCacheTest {

  @Mock
  private Clock clock;

  @InjectMocks
  private BookCache underTest;


  @Test
  void shouldReturnBookWhenPresentAndNotExpired() {
    // given
    var isbn = "isbn";
    var book = mock(Book.class);
    when(clock.millis()).thenReturn(1000L).thenReturn(1001L);
    when(clock.instant()).thenReturn(Instant.ofEpochMilli(1000L)).thenReturn(Instant.ofEpochMilli(1001L));
    underTest.put(isbn, book);

    // when
    Optional<Book> result = underTest.get(isbn);

    // then
    assertThat(result).isPresent().contains(book);
  }

  @Test
  void shouldReturnEmptyWhenBookNotPresent() {
    // given
    var isbn = "isbn";

    // when
    Optional<Book> result = underTest.get(isbn);

    // then
    assertThat(result).isNotPresent();
  }

  @Test
  void shouldReturnEmptyWhenBookExpired() {
    // given
    var isbn = "isbn";
    var book = mock(Book.class);
    when(clock.millis()).thenReturn(5000L);
    when(clock.instant()).thenReturn(Instant.ofEpochMilli(1000L));
    underTest.put(isbn, book);

    // when
    Optional<Book> result = underTest.get(isbn);

    // then
    assertThat(result).isNotPresent();
  }

  @Test
  void shouldRemoveBookFromCache() {
    // given
    var isbn = "isbn";
    var book = mock(Book.class);
    when(clock.instant()).thenReturn(Instant.ofEpochMilli(1000L));
    underTest.put(isbn, book);

    // when
    underTest.remove(isbn);
    Optional<Book> result = underTest.get(isbn);

    // then
    assertThat(result).isNotPresent();
  }

  @Test
  void shouldEvictStaleBooks() {
    // given
    var isbn1 = "isbn1";
    var isbn2 = "isbn2";
    var book1 = mock(Book.class);
    var book2 = mock(Book.class);
    when(clock.millis()).thenReturn(3500L);
    when(clock.instant()).thenReturn(Instant.ofEpochMilli(1000L)).thenReturn(Instant.ofEpochMilli(2000L));
    underTest.put(isbn1, book1);
    underTest.put(isbn2, book2);

    // when
    underTest.evictStaleBooks();
    Optional<Book> result1 = underTest.get(isbn1);
    Optional<Book> result2 = underTest.get(isbn2);

    // then
    assertThat(result1).isNotPresent();
    assertThat(result2).isPresent().contains(book2);
  }

  @Test
  void shouldRefreshTtlWhenBookAccessed() {
    // given
    var isbn = "isbn";
    var book = mock(Book.class);
    when(clock.millis()).thenReturn(1000L).thenReturn(2000L);
    when(clock.instant()).thenReturn(Instant.ofEpochMilli(1000L)).thenReturn(Instant.ofEpochMilli(1001L));
    underTest.put(isbn, book);

    // Access the book to refresh TTL
    underTest.get(isbn);

    // when
    Optional<Book> result = underTest.get(isbn);

    // then
    assertThat(result).isPresent().contains(book);
  }
}