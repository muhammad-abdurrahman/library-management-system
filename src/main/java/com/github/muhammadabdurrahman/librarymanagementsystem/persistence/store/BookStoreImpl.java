package com.github.muhammadabdurrahman.librarymanagementsystem.persistence.store;

import com.github.muhammadabdurrahman.librarymanagementsystem.business.exception.BookAlreadyExistsException;
import com.github.muhammadabdurrahman.librarymanagementsystem.business.exception.BookNotFoundException;
import com.github.muhammadabdurrahman.librarymanagementsystem.business.exception.InsufficientNumberOfAvailableCopiesException;
import com.github.muhammadabdurrahman.librarymanagementsystem.business.model.Book;
import com.github.muhammadabdurrahman.librarymanagementsystem.business.store.BookStore;
import com.github.muhammadabdurrahman.librarymanagementsystem.concurrency.annotation.LockKey;
import com.github.muhammadabdurrahman.librarymanagementsystem.concurrency.annotation.SynchronizedByReentrantLock;
import com.github.muhammadabdurrahman.librarymanagementsystem.persistence.cache.BookCache;
import com.github.muhammadabdurrahman.librarymanagementsystem.persistence.model.BookEntity;
import com.github.muhammadabdurrahman.librarymanagementsystem.persistence.model.mapper.BookEntityMapper;
import com.github.muhammadabdurrahman.librarymanagementsystem.persistence.repository.BookJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class BookStoreImpl implements BookStore {

  private final BookJpaRepository repository;
  private final BookCache cache;
  private final BookEntityMapper mapper;

  @SynchronizedByReentrantLock
  @Override
  public void addBook(@LockKey Book book) {
    if (repository.existsById(book.getIsbn())) {
      throw new BookAlreadyExistsException(book.getIsbn());
    }
    repository.saveAndFlush(mapper.map(book));
    cache.put(book.getIsbn(), book);
  }

  @SynchronizedByReentrantLock
  @Override
  public void removeBook(@LockKey String isbn) {
    if (!repository.existsById(isbn)) {
      throw new BookNotFoundException(isbn);
    }
    repository.deleteById(isbn);
    repository.flush();
    cache.remove(isbn);
  }

  @Override
  public Optional<Book> findBookByIsbn(String isbn) {
    return cache.get(isbn).or(() -> {
      Optional<Book> book = repository.findById(isbn).map(mapper::map);
      book.ifPresent(b -> cache.put(isbn, b));
      return book;
    });
  }

  @Override
  public List<Book> findBooksByAuthor(String author) {
    return mapper.map(repository.findAllByAuthor(author));
  }

  @SynchronizedByReentrantLock
  @Override
  public void borrowBook(@LockKey String isbn) {
    BookEntity book = repository.findByIdForUpdate(isbn)
        .orElseThrow(() -> new BookNotFoundException(isbn));
    if (book.getAvailableCopies() == 0) {
      throw new InsufficientNumberOfAvailableCopiesException(isbn);
    }
    book.setAvailableCopies(book.getAvailableCopies() - 1);
    repository.save(book);
    cache.remove(isbn);
  }

  @SynchronizedByReentrantLock
  @Override
  public void returnBook(@LockKey String isbn) {
    BookEntity book = repository.findByIdForUpdate(isbn)
        .orElseThrow(() -> new BookNotFoundException(isbn));
    book.setAvailableCopies(book.getAvailableCopies() + 1);
    repository.save(book);
    cache.remove(isbn);
  }
}