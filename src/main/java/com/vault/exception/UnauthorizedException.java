package com.vault.exception;

/**
 * Exception thrown when a user tries to access a resource without permission.
 */
public class UnauthorizedException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = -5304593966332975561L;

	public UnauthorizedException(String message) {
        super(message);
    }
}
