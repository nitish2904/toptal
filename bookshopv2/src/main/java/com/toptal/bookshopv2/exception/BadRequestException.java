package com.toptal.bookshopv2.exception;
/** Thrown when a client request violates business rules (HTTP 400). */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
