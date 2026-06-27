package com.yearis.blog_application.exception;

import com.yearis.blog_application.payload.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Our Exception Handler for ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception, WebRequest webRequest) {

        // create a new ErrorResponse
        ErrorResponse error = new ErrorResponse();

        error.setStatusCode(HttpStatus.NOT_FOUND.value());
        error.setMessage(exception.getMessage());
        error.setTimeStamp(LocalDateTime.now());
        error.setDetails(webRequest.getDescription(false));

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // Our Exception Handler for BlogAPIException
    @ExceptionHandler(BlogAPIException.class)
    public ResponseEntity<ErrorResponse> handleBlogAPIException(BlogAPIException exception, WebRequest webRequest) {

        // create a new ErrorResponse
        ErrorResponse error = new ErrorResponse();

        error.setStatusCode(exception.getStatus().value());
        error.setMessage(exception.getMessage());
        error.setTimeStamp(LocalDateTime.now());
        error.setDetails(webRequest.getDescription(false));

        return new ResponseEntity<>(error, exception.getStatus());
    }
}
