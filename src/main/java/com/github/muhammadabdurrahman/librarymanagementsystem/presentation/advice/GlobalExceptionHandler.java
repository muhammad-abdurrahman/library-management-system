package com.github.muhammadabdurrahman.librarymanagementsystem.presentation.advice;

import com.github.muhammadabdurrahman.librarymanagementsystem.business.exception.BookAlreadyExistsException;
import com.github.muhammadabdurrahman.librarymanagementsystem.business.exception.BookNotFoundException;
import com.github.muhammadabdurrahman.librarymanagementsystem.business.exception.InsufficientNumberOfAvailableCopiesException;
import com.github.muhammadabdurrahman.librarymanagementsystem.presentation.exception.CustomProblem;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BookNotFoundException.class)
  public ResponseEntity<Problem> handleBookNotFoundException(BookNotFoundException ex, NativeWebRequest request) {
    var problem = CustomProblem.builder()
        .title("Book not found")
        .detail(ex.getMessage())
        .status(Status.NOT_FOUND)
        .message("Book with the given ISBN was not found")
        .build();
    return ResponseEntity.status(Status.NOT_FOUND.getStatusCode()).body(problem);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Problem> handleNoResourceFound(NoResourceFoundException ex, NativeWebRequest request) {
    var problem = CustomProblem.builder()
        .title("The requested resource was not found")
        .detail(ex.getMessage())
        .status(Status.NOT_FOUND)
        .message("The requested resource was not found")
        .build();
    return ResponseEntity.status(Status.NOT_FOUND.getStatusCode()).body(problem);
  }

  @ExceptionHandler(BookAlreadyExistsException.class)
  public ResponseEntity<Problem> handleBookAlreadyExistsException(BookAlreadyExistsException ex, NativeWebRequest request) {
    var problem = CustomProblem.builder()
        .title("Book already exists")
        .detail(ex.getMessage())
        .status(Status.CONFLICT)
        .message("A book with the given ISBN already exists")
        .build();
    return ResponseEntity.status(Status.CONFLICT.getStatusCode()).body(problem);
  }

  @ExceptionHandler(InsufficientNumberOfAvailableCopiesException.class)
  public ResponseEntity<Problem> handleInsufficientNumberOfAvailableCopiesException(InsufficientNumberOfAvailableCopiesException ex, NativeWebRequest request) {
    var problem = CustomProblem.builder()
        .title("Insufficient number of available copies of book")
        .detail(ex.getMessage())
        .status(Status.CONFLICT)
        .message("Not enough copies of the book are available")
        .build();
    return ResponseEntity.status(Status.CONFLICT.getStatusCode()).body(problem);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Problem> handleValidationExceptions(MethodArgumentNotValidException ex, NativeWebRequest request) {
    final Set<String> validationErrors = new HashSet<>();
    ex.getBindingResult().getFieldErrors()
        .forEach(error -> validationErrors.add("[field: %s, error: %s]".formatted(error.getField(), error.getDefaultMessage())));

    var problem = CustomProblem.builder()
        .title("Validation errors occurred")
        .detail(String.join(", ", validationErrors))
        .status(Status.BAD_REQUEST)
        .message("Validation failed for one or more fields")
        .build();

    return ResponseEntity.badRequest().body(problem);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Problem> handleGenericException(Exception ex, NativeWebRequest request) {
    log.error("An unexpected error occurred", ex);
    var problem = CustomProblem.builder()
        .title("Internal Server Error")
        .detail("An unexpected error occurred: %s".formatted(ex.getMessage()))
        .status(Status.INTERNAL_SERVER_ERROR)
        .message("An unexpected error occurred")
        .build();
    return ResponseEntity.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).body(problem);
  }
}