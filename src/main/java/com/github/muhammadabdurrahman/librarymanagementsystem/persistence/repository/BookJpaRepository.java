package com.github.muhammadabdurrahman.librarymanagementsystem.persistence.repository;

import com.github.muhammadabdurrahman.librarymanagementsystem.persistence.model.BookEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookJpaRepository extends JpaRepository<BookEntity, String> {

  List<BookEntity> findAllByAuthor(String author);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT b FROM BookEntity b WHERE b.isbn = :isbn")
  Optional<BookEntity> findByIdForUpdate(@Param("isbn") String isbn);
}