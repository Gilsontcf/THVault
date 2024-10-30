package com.vault.exception;

/**
 * Exception thrown when a user tries to access a resource without permission.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
