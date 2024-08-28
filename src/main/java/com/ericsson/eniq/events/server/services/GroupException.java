package com.ericsson.eniq.events.server.services;

/**
 * Generic exception used during the group stuff.
 *
 * @author eeipca
 *
 */
public class GroupException extends RuntimeException {
    /**
     * Constructor with a message
     * @param message Error message
     */
    public GroupException(final String message) {
        super(message);
    }

    /**
     * Constructor with a message
     * @param cause Original cause
     */
    public GroupException(final Throwable cause) {
        super(cause);
    }
}
