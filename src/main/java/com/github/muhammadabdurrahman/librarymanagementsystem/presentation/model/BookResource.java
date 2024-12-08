package com.github.muhammadabdurrahman.librarymanagementsystem.presentation.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record BookResource(
    @NotBlank(message = "ISBN is required")
    @Pattern(
        regexp = "^(?:ISBN(?:-1[03])?:?\\s*)?(?=[-0-9X]{10,17}$|[-0-9X]{13}$)(97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$",
        message = "Invalid ISBN format"
    )
    String isbn,

    @NotBlank(message = "Title is required")
    String title,

    @NotBlank(message = "Author is required")
    String author,

    @NotNull(message = "Publication year is required")
    @Min(value = 1000, message = "Publication year must be a valid year")
    @Max(value = 9999, message = "Publication year must be a valid year")
    Integer publicationYear,

    @NotNull(message = "Available copies is required")
    @Min(value = 0, message = "Available copies cannot be less than 0")
    Integer availableCopies
) {

}