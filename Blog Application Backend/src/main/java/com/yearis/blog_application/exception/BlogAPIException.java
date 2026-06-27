package com.yearis.blog_application.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BlogAPIException extends RuntimeException {

    private HttpStatus status;
    private String message;

    public BlogAPIException(HttpStatus status, String message) {
        super(message);

        // store the fields
        this.status = status;
        this.message = message;
    }
}
