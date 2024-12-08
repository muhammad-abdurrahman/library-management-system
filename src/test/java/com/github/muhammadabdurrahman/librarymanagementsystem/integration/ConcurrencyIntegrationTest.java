package com.github.muhammadabdurrahman.librarymanagementsystem.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.muhammadabdurrahman.librarymanagementsystem.persistence.repository.BookJpaRepository;
import com.github.muhammadabdurrahman.librarymanagementsystem.presentation.model.BookResource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
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
class ConcurrencyIntegrationTest {

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
  void should_handle_concurrent_borrow_requests() throws Exception {
    int numberOfThreads = 50;
    AtomicInteger successCount;
    AtomicInteger failureCount;
    try (ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads)) {
      CountDownLatch latch = new CountDownLatch(numberOfThreads);
      successCount = new AtomicInteger();
      failureCount = new AtomicInteger();

      for (int i = 0; i < numberOfThreads; i++) {
        final int threadIndex = i;
        executorService.submit(() -> {
          try {
            mockMvc.perform(put("/api/v1/books/{isbn}/borrow", "978-0-596-52068-1")
                    .header("X-Forwarded-For", "192.168.0." + threadIndex))
                .andExpect(result -> {
                  if (result.getResponse().getStatus() == 200) {
                    successCount.incrementAndGet();
                  } else if (result.getResponse().getStatus() == 409) {
                    failureCount.incrementAndGet();
                  }
                });
          } catch (Exception e) {
            log.error("Error occurred", e);
          } finally {
            latch.countDown();
          }
        });
      }

      latch.await();
      executorService.shutdown();
    }

    SoftAssertions.assertSoftly(softly -> {
      assertThat(bookJpaRepository.findById("978-0-596-52068-1").orElseThrow().getAvailableCopies()).isZero();
      softly.assertThat(successCount.get()).isEqualTo(10);
      softly.assertThat(failureCount.get()).isEqualTo(40);
    });
  }

  @Test
  void should_handle_concurrent_return_requests() throws Exception {
    var availableCopies = bookJpaRepository.findById("978-0-596-52068-1").orElseThrow().getAvailableCopies();
    int numberOfThreads = 50;
    AtomicInteger successCount;
    AtomicInteger failureCount;
    try (ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads)) {
      CountDownLatch latch = new CountDownLatch(numberOfThreads);
      successCount = new AtomicInteger();
      failureCount = new AtomicInteger();

      for (int i = 0; i < numberOfThreads; i++) {
        final int threadIndex = i;
        executorService.submit(() -> {
          try {
            mockMvc.perform(put("/api/v1/books/{isbn}/return", "978-0-596-52068-1")
                    .header("X-Forwarded-For", "192.168.0." + threadIndex))
                .andExpect(result -> {
                  if (result.getResponse().getStatus() == 200) {
                    successCount.incrementAndGet();
                  } else if (result.getResponse().getStatus() == 409) {
                    failureCount.incrementAndGet();
                  }
                });
          } catch (Exception e) {
            log.error("Error occurred", e);
          } finally {
            latch.countDown();
          }
        });
      }

      latch.await();
      executorService.shutdown();
    }

    SoftAssertions.assertSoftly(softly -> {
      assertThat(bookJpaRepository.findById("978-0-596-52068-1").orElseThrow().getAvailableCopies()).isEqualTo(availableCopies + numberOfThreads);
      softly.assertThat(successCount.get()).isEqualTo(50);
      softly.assertThat(failureCount.get()).isZero();
    });
  }

  @Test
  void should_handle_concurrent_add_book_requests() throws Exception {
    int numberOfThreads = 50;
    AtomicInteger successCount;
    AtomicInteger failureCount;
    try (ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads)) {
      CountDownLatch latch = new CountDownLatch(numberOfThreads);
      successCount = new AtomicInteger();
      failureCount = new AtomicInteger();

      for (int i = 0; i < numberOfThreads; i++) {
        final int threadIndex = i;
        executorService.submit(() -> {
          try {
            String content = objectMapper.writeValueAsString(testBook);
            mockMvc.perform(post("/api/v1/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
                    .header("X-Forwarded-For", "192.168.0." + threadIndex))
                .andExpect(result -> {
                  if (result.getResponse().getStatus() == 201) {
                    successCount.incrementAndGet();
                  } else if (result.getResponse().getStatus() == 409) {
                    failureCount.incrementAndGet();
                  }
                });
          } catch (Exception e) {
            log.error("Error occurred", e);
          } finally {
            latch.countDown();
          }
        });
      }

      latch.await();
      executorService.shutdown();
    }

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(successCount.get()).isEqualTo(1);
      softly.assertThat(failureCount.get()).isEqualTo(49);
    });
  }
}