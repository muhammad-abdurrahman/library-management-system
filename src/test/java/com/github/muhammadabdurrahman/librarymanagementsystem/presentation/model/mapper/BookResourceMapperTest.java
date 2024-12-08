package com.github.muhammadabdurrahman.librarymanagementsystem.presentation.model.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.muhammadabdurrahman.librarymanagementsystem.business.model.Book;
import com.github.muhammadabdurrahman.librarymanagementsystem.presentation.model.BookResource;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class BookResourceMapperTest {

  private BookResourceMapper underTest;

  @BeforeEach
  void setUp() {
    underTest = Mappers.getMapper(BookResourceMapper.class);
  }

  @Test
  void shouldMapBookToBookResource() {
    // given
    var expected = Book.builder()
        .isbn("isbn")
        .title("title")
        .author("author")
        .publicationYear(2017)
        .availableCopies(5)
        .build();

    // when
    BookResource actual = underTest.map(expected);

    // then
    assertThat(actual)
        .usingRecursiveComparison()
        .isEqualTo(expected);
  }

  @Test
  void shouldMapBookResourceToBook() {
    // given
    var expected = new BookResource("isbn", "title", "author", 2017, 5);

    // when
    Book actual = underTest.map(expected);

    // then
    assertThat(actual)
        .usingRecursiveComparison()
        .isEqualTo(expected);
  }

  @Test
  void shouldMapBooksToBookResources() {
    // given
    var book1 = Book.builder()
        .isbn("isbn1")
        .title("title1")
        .author("author1")
        .publicationYear(2017)
        .availableCopies(5)
        .build();
    var book2 = Book.builder()
        .isbn("isbn2")
        .title("title2")
        .author("author2")
        .publicationYear(2017)
        .availableCopies(3)
        .build();
    List<Book> expected = List.of(book1, book2);

    // when
    List<BookResource> actual = underTest.map(expected);

    // then
    assertThat(actual)
        .usingRecursiveComparison()
        .isEqualTo(expected);
  }
}