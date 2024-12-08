package com.github.muhammadabdurrahman.librarymanagementsystem.persistence.model.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.muhammadabdurrahman.librarymanagementsystem.business.model.Book;
import com.github.muhammadabdurrahman.librarymanagementsystem.persistence.model.BookEntity;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class BookEntityMapperTest {

  private BookEntityMapper underTest;

  @BeforeEach
  void setUp() {
    underTest = Mappers.getMapper(BookEntityMapper.class);
  }

  @Test
  void shouldMapBookToBookEntity() {
    // given
    var expected = Book.builder()
        .isbn("isbn")
        .title("title")
        .author("author")
        .publicationYear(2017)
        .availableCopies(5)
        .build();

    // when
    BookEntity actual = underTest.map(expected);

    // then
    assertThat(actual)
        .usingRecursiveComparison()
        .isEqualTo(expected);
  }

  @Test
  void shouldMapBookEntityToBook() {
    // given
    var expected = new BookEntity("isbn", "title", "author", 2017, 5);

    // when
    Book actual = underTest.map(expected);

    // then
    assertThat(actual)
        .usingRecursiveComparison()
        .isEqualTo(expected);
  }

  @Test
  void shouldMapBookEntitiesToBooks() {
    // given
    var bookEntity1 = new BookEntity("isbn1", "title1", "author1", 2017, 5);
    var bookEntity2 = new BookEntity("isbn2", "title2", "author2", 2017, 3);
    List<BookEntity> expected = List.of(bookEntity1, bookEntity2);

    // when
    List<Book> actual = underTest.map(expected);

    // then
    assertThat(actual)
        .usingRecursiveComparison()
        .isEqualTo(expected);
  }
}