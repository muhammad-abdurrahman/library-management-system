package com.github.muhammadabdurrahman.librarymanagementsystem.presentation.model.mapper;

import com.github.muhammadabdurrahman.librarymanagementsystem.business.model.Book;
import com.github.muhammadabdurrahman.librarymanagementsystem.presentation.model.BookResource;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper
public interface BookResourceMapper {

  BookResource map(Book book);

  List<BookResource> map(List<Book> books);

  Book map(BookResource bookResource);

}
