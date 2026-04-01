package com.toptal.bookshopv2.exception;
/** Thrown when a requested resource (book, category, user, etc.) does not exist (HTTP 404). */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}
