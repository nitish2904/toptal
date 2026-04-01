package com.toptal.bookshop.exception;
/** Thrown when a request conflicts with the current state, e.g. duplicate email (HTTP 409). */

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
