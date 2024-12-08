package com.github.muhammadabdurrahman.librarymanagementsystem.presentation.controller;

import com.github.muhammadabdurrahman.librarymanagementsystem.presentation.model.BookResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name = "Books", description = "Operations related to books in the library")
@RequestMapping("/api/v1/books")
public interface BookApi {

  @GetMapping(value = "/{isbn}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.OK)
  @Operation(
      summary = "Get a book by ISBN",
      description = "Retrieve a book's details by its ISBN",
      responses = {
          @ApiResponse(responseCode = "200", description = "Book found",
              content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResource.class))),
          @ApiResponse(responseCode = "404", description = "Book not found",
              content = @Content)
      })
  BookResource findBookByIsbn(@PathVariable String isbn);

  @GetMapping(value = "/author/{author}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.OK)
  @Operation(
      summary = "Get books by author",
      description = "Retrieve a list of books by the author's name",
      responses = {
          @ApiResponse(responseCode = "200", description = "Books found",
              content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResource.class))),
          @ApiResponse(responseCode = "404", description = "No books found for the author",
              content = @Content)
      })
  List<BookResource> findBooksByAuthor(@PathVariable String author);

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Add a new book",
      description = "Add a new book to the library",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Book details to be added",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResource.class),
              examples = @ExampleObject(value = "{ \"isbn\": \"978-0-596-52068-1\", \"title\": \"The Hobbit\", \"author\": \"J. R. R. Tolkien\", \"publicationYear\": 2001, \"availableCopies\": 5 }"))
      ),
      responses = {
          @ApiResponse(responseCode = "201", description = "Book added successfully"),
          @ApiResponse(responseCode = "400", description = "Invalid payload",
              content = @Content)
      })
  void addBook(@RequestBody @Valid BookResource book);

  @PutMapping(value = "/{isbn}/borrow", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.OK)
  @Operation(
      summary = "Borrow a book",
      description = "Borrow a book from the library by updating the available copies",
      responses = {
          @ApiResponse(responseCode = "200", description = "Book borrowed successfully"),
          @ApiResponse(responseCode = "404", description = "Book not found",
              content = @Content)
      })
  void borrowBook(@PathVariable String isbn);

  @PutMapping(value = "/{isbn}/return", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.OK)
  @Operation(
      summary = "Return a book",
      description = "Return a borrowed book to the library",
      responses = {
          @ApiResponse(responseCode = "200", description = "Book returned successfully"),
          @ApiResponse(responseCode = "404", description = "Book not found",
              content = @Content)
      })
  void returnBook(@PathVariable String isbn);

  @DeleteMapping(value = "/{isbn}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Remove a book",
      description = "Remove a book from the library by its ISBN",
      responses = {
          @ApiResponse(responseCode = "204", description = "Book removed successfully"),
          @ApiResponse(responseCode = "404", description = "Book not found",
              content = @Content)
      })
  void removeBook(@PathVariable String isbn);
}

