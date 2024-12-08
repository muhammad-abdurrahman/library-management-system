package com.github.muhammadabdurrahman.librarymanagementsystem.persistence.cache;

import com.github.muhammadabdurrahman.librarymanagementsystem.business.model.Book;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookCache {

  private final ConcurrentHashMap<String, BookCacheItem> cache = new ConcurrentHashMap<>();
  private final Clock clock;

  public Optional<Book> get(String isbn) {
    return Optional.ofNullable(cache.get(isbn))
        .filter(item -> item.getTtl() > clock.millis())
        .map(item -> {
          item.refreshTtl(clock);
          return item.getBook();
        });
  }

  public void put(String isbn, Book book) {
    cache.put(isbn, new BookCacheItem(book, clock));
  }

  public void remove(String isbn) {
    cache.remove(isbn);
  }

  public void evictStaleBooks() {
    long currentTimeMs = clock.millis();
    cache.forEach((key, value) -> {
      if (isExpired(value.ttl, currentTimeMs)) {
        cache.computeIfPresent(key, (k, v) -> isExpired(v.ttl, currentTimeMs) ? null : v);
      }
    });
  }

  private boolean isExpired(long ttl, long currentTime) {
    return ttl <= currentTime;
  }

  @Getter
  public static class BookCacheItem {

    public static final Duration TTL_DURATION = Duration.ofSeconds(2);

    private final Book book;
    private Long ttl;

    public BookCacheItem(Book book, Clock clock) {
      this.book = book;
      refreshTtl(clock);
    }

    public void refreshTtl(Clock clock) {
      this.ttl = Instant.now(clock).toEpochMilli() + TTL_DURATION.toMillis();
    }
  }
}