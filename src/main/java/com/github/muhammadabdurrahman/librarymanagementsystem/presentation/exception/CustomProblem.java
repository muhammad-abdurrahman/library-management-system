package com.github.muhammadabdurrahman.librarymanagementsystem.presentation.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = {"cause", "stackTrace", "type", "parameters", "suppressed"})
@Getter
@Builder
public class CustomProblem extends AbstractThrowableProblem {

  private final String title;
  private final Status status;
  private final String detail;
  private final String message;

  public CustomProblem(String title, Status status, String detail, String message) {
    this.title = title;
    this.status = status;
    this.detail = detail;
    this.message = message;
  }
}
