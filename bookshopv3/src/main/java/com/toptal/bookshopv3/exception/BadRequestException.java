/** Thrown when a client request violates business rules (HTTP 400). */
package com.toptal.bookshopv3.exception;
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
