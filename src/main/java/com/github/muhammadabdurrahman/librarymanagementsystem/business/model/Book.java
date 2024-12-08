package com.github.muhammadabdurrahman.librarymanagementsystem.business.model;

import com.github.muhammadabdurrahman.librarymanagementsystem.concurrency.model.Lockable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class Book implements Lockable {

  private final String isbn;
  private final String title;
  private final String author;
  private final Integer publicationYear;
  private final Integer availableCopies;

  @Override
  public Object getLockKey() {
    return this.isbn;
  }
}
