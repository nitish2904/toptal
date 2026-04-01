/** Thrown when a request conflicts with the current state, e.g. duplicate email (HTTP 409). */
package com.toptal.bookshopv3.exception;
public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
