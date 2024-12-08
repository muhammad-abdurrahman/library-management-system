package com.github.muhammadabdurrahman.librarymanagementsystem.persistence.model.mapper;

import com.github.muhammadabdurrahman.librarymanagementsystem.business.model.Book;
import com.github.muhammadabdurrahman.librarymanagementsystem.persistence.model.BookEntity;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper
public interface BookEntityMapper {

  BookEntity map(Book book);

  Book map(BookEntity bookEntity);

  List<Book> map(List<BookEntity> bookEntities);
}