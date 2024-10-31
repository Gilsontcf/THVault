package com.vault.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 2497714230666133509L;

	public ResourceNotFoundException(String message) {
        super(message);
    }
}
