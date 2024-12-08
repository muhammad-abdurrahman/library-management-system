package com.github.muhammadabdurrahman.librarymanagementsystem.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/test-data.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RateLimitIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void should_rate_limit_requests() throws Exception {
    int numberOfThreads = 70;
    AtomicInteger successCount;
    AtomicInteger failureCount;
    try (ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads)) {
      CountDownLatch latch = new CountDownLatch(numberOfThreads);
      successCount = new AtomicInteger();
      failureCount = new AtomicInteger();

      for (int i = 0; i < numberOfThreads; i++) {
        executorService.submit(() -> {
          try {
            mockMvc.perform(get("/api/v1/books/{isbn}", "978-0-596-52068-1"))
                .andExpect(result -> {
                  if (result.getResponse().getStatus() == 200) {
                    successCount.incrementAndGet();
                  } else if (result.getResponse().getStatus() == 429) {
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
      softly.assertThat(successCount.get()).isEqualTo(60);
      softly.assertThat(failureCount.get()).isEqualTo(10);
    });
  }
}