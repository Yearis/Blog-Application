package com.yearis.blog_application.exception;

import com.yearis.blog_application.exception.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException exception, WebRequest webRequest) {

        // create a new ErrorResponse
        ErrorResponse error = new ErrorResponse();

        error.setStatusCode(HttpStatus.BAD_REQUEST.value());
        error.setMessage(exception.getMessage());
        error.setTimeStamp(LocalDateTime.now());
        error.setDetails(webRequest.getDescription(false));

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ResourceConflictException exception, WebRequest webRequest) {

        // create a new ErrorResponse
        ErrorResponse error = new ErrorResponse();

        error.setStatusCode(HttpStatus.CONFLICT.value());
        error.setMessage(exception.getMessage());
        error.setTimeStamp(LocalDateTime.now());
        error.setDetails(webRequest.getDescription(false));

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }


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

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccessException(UnauthorizedAccessException exception, WebRequest webRequest) {

        // create a new ErrorResponse
        ErrorResponse error = new ErrorResponse();

        error.setStatusCode(HttpStatus.FORBIDDEN.value());
        error.setMessage(exception.getMessage());
        error.setTimeStamp(LocalDateTime.now());
        error.setDetails(webRequest.getDescription(false));

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UnAuthenticatedException.class)
    public ResponseEntity<ErrorResponse> handleUnAuthenticatedException(UnAuthenticatedException exception, WebRequest webRequest) {

        // create a new ErrorResponse
        ErrorResponse error = new ErrorResponse();

        error.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        error.setMessage(exception.getMessage());
        error.setTimeStamp(LocalDateTime.now());
        error.setDetails(webRequest.getDescription(false));

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }
}
