/** Thrown when a requested resource does not exist (HTTP 404). */
package com.toptal.bookshopv3.exception;
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}
