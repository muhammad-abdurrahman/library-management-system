package com.github.muhammadabdurrahman.librarymanagementsystem.schedule;

import com.github.muhammadabdurrahman.librarymanagementsystem.persistence.cache.BookCache;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheEvictionScheduledTask {

  private final BookCache bookCache;

  @Scheduled(fixedRateString = "${cache.eviction.fixedRate}")
  public void scheduledEviction() {
    bookCache.evictStaleBooks();
  }
}
