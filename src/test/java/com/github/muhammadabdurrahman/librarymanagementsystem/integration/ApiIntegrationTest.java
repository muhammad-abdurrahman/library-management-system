package com.github.muhammadabdurrahman.librarymanagementsystem.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.muhammadabdurrahman.librarymanagementsystem.persistence.repository.BookJpaRepository;
import com.github.muhammadabdurrahman.librarymanagementsystem.presentation.model.BookResource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/test-data.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private BookJpaRepository bookJpaRepository;

  private BookResource testBook;

  @BeforeEach
  void setUp() {
    testBook = BookResource.builder()
        .isbn("978-0-596-52068-7")
        .title("Test Book")
        .author("Test Author")
        .publicationYear(2023)
        .availableCopies(15)
        .build();
  }

  @Test
  void should_find_book_by_isbn() throws Exception {
    mockMvc.perform(get("/api/v1/books/{isbn}", "978-0-596-52068-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isbn").value("978-0-596-52068-1"))
        .andExpect(jsonPath("$.title").value("Book 1"))
        .andExpect(jsonPath("$.author").value("Muhammad Abdurrahman"))
        .andExpect(jsonPath("$.availableCopies").value(10));
  }

  @Test
  void should_return_not_found_for_non_existent_book() throws Exception {
    mockMvc.perform(get("/api/v1/books/{isbn}", "999-9-9999-9999-9"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Book not found"))
        .andExpect(jsonPath("$.status").value("NOT_FOUND"))
        .andExpect(jsonPath("$.detail").value("Book with ISBN 999-9-9999-9999-9 not found"))
        .andExpect(jsonPath("$.message").value("Book with the given ISBN was not found"));
  }

  @Test
  void should_find_books_by_author() throws Exception {
    mockMvc.perform(get("/api/v1/books/author/{author}", "Muhammad Abdurrahman"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].author").value("Muhammad Abdurrahman"))
        .andExpect(jsonPath("$[1].author").value("Muhammad Abdurrahman"));
  }

  @Test
  void should_add_new_book() throws Exception {
    String content = objectMapper.writeValueAsString(testBook);
    mockMvc.perform(post("/api/v1/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(content))
        .andExpect(status().isCreated());

    assertThat(bookJpaRepository.findById("978-0-596-52068-7"))
        .isPresent()
        .get()
        .usingRecursiveComparison()
        .isEqualTo(testBook);
  }

  @Test
  void should_handle_validation_failures_when_add_new_book() throws Exception {
    BookResource invalidBook = BookResource.builder()
        .isbn("invalid-isbn")
        .title("")
        .author("")
        .publicationYear(999)
        .availableCopies(-1)
        .build();

    mockMvc.perform(post("/api/v1/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidBook)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Validation errors occurred"))
        .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").value("Validation failed for one or more fields"))
        .andExpect(jsonPath("$.detail").value(containsString("[field: isbn, error: Invalid ISBN format]")))
        .andExpect(jsonPath("$.detail").value(containsString("[field: title, error: Title is required]")))
        .andExpect(jsonPath("$.detail").value(containsString("[field: author, error: Author is required]")))
        .andExpect(jsonPath("$.detail").value(containsString("[field: publicationYear, error: Publication year must be a valid year]")))
        .andExpect(jsonPath("$.detail").value(containsString("[field: availableCopies, error: Available copies cannot be less than 0]")));
  }

  @Test
  void should_handle_duplicate_book_error_when_add_new_book() throws Exception {
    mockMvc.perform(post("/api/v1/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testBook)))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/api/v1/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testBook)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.title").value("Book already exists"))
        .andExpect(jsonPath("$.status").value("CONFLICT"))
        .andExpect(jsonPath("$.detail").value("Book with ISBN 978-0-596-52068-7 already exists"))
        .andExpect(jsonPath("$.message").value("A book with the given ISBN already exists"));
  }

  @Test
  void should_borrow_book() throws Exception {
    Integer availableCopies = bookJpaRepository.findById("978-0-596-52068-3").orElseThrow().getAvailableCopies();
    mockMvc.perform(put("/api/v1/books/{isbn}/borrow", "978-0-596-52068-3"))
        .andExpect(status().isOk());
    assertThat(bookJpaRepository.findById("978-0-596-52068-3").orElseThrow().getAvailableCopies()).isEqualTo(availableCopies - 1);
  }

  @Test
  void should_handle_borrow_book_error_when_no_copies_available() throws Exception {
    mockMvc.perform(put("/api/v1/books/{isbn}/borrow", "978-0-596-52068-6"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.title").value("Insufficient number of available copies of book"))
        .andExpect(jsonPath("$.status").value("CONFLICT"))
        .andExpect(jsonPath("$.detail").value("Insufficient number of available copies for book with ISBN 978-0-596-52068-6"))
        .andExpect(jsonPath("$.message").value("Not enough copies of the book are available"));
  }

  @Test
  void should_handle_borrow_book_error_when_book_not_found() throws Exception {
    mockMvc.perform(put("/api/v1/books/{isbn}/borrow", "978-0-596-52068-10"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Book not found"))
        .andExpect(jsonPath("$.status").value("NOT_FOUND"))
        .andExpect(jsonPath("$.detail").value("Book with ISBN 978-0-596-52068-10 not found"))
        .andExpect(jsonPath("$.message").value("Book with the given ISBN was not found"));
  }

  @Test
  void should_return_book() throws Exception {
    var availableCopies = bookJpaRepository.findById("978-0-596-52068-4").orElseThrow().getAvailableCopies();
    mockMvc.perform(put("/api/v1/books/{isbn}/return", "978-0-596-52068-4"))
        .andExpect(status().isOk());
    assertThat(bookJpaRepository.findById("978-0-596-52068-4").orElseThrow().getAvailableCopies()).isEqualTo(availableCopies + 1);
  }

  @Test
  void should_handle_return_book_error_when_book_not_found() throws Exception {
    mockMvc.perform(put("/api/v1/books/{isbn}/return", "978-0-596-52068-10"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Book not found"))
        .andExpect(jsonPath("$.status").value("NOT_FOUND"))
        .andExpect(jsonPath("$.detail").value("Book with ISBN 978-0-596-52068-10 not found"))
        .andExpect(jsonPath("$.message").value("Book with the given ISBN was not found"));
  }

  @Test
  void should_remove_book() throws Exception {
    assertThat(bookJpaRepository.findById("978-0-596-52068-5")).isNotEmpty();
    mockMvc.perform(delete("/api/v1/books/{isbn}", "978-0-596-52068-5"))
        .andExpect(status().isNoContent());
    assertThat(bookJpaRepository.findById("978-0-596-52068-5")).isEmpty();
  }

  @Test
  void should_handle_remove_book_error_for_non_existent_book() throws Exception {
    mockMvc.perform(delete("/api/v1/books/{isbn}", "999-9-9999-9999-9"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Book not found"))
        .andExpect(jsonPath("$.status").value("NOT_FOUND"))
        .andExpect(jsonPath("$.detail").value("Book with ISBN 999-9-9999-9999-9 not found"))
        .andExpect(jsonPath("$.message").value("Book with the given ISBN was not found"));
  }

  @Test
  void should_handle_no_resource_found_error() throws Exception {
    mockMvc.perform(get("/api/v1/invalid"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("The requested resource was not found"))
        .andExpect(jsonPath("$.status").value("NOT_FOUND"))
        .andExpect(jsonPath("$.detail").value("No static resource api/v1/invalid."))
        .andExpect(jsonPath("$.message").value("The requested resource was not found"));
  }
}