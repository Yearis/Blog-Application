package com.yearis.blog_application.exception;

public class UnAuthenticatedException extends RuntimeException {
    public UnAuthenticatedException(String message) {
        super(message);
    }
}
