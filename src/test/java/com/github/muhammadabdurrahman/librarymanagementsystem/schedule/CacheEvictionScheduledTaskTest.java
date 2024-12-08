package com.github.muhammadabdurrahman.librarymanagementsystem.schedule;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import com.github.muhammadabdurrahman.librarymanagementsystem.persistence.cache.BookCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CacheEvictionScheduledTaskTest {

  @Mock
  private BookCache bookCache;

  @InjectMocks
  private CacheEvictionScheduledTask underTest;

  @Test
  void shouldEvictStaleBooks() {
    // given
    doNothing().when(bookCache).evictStaleBooks();

    // when
    underTest.scheduledEviction();

    // then
    verify(bookCache).evictStaleBooks();
  }

}