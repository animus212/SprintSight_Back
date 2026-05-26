package com.example.sprintsight.exceptions;

import java.io.Serial;

public class ResourceConflictException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public ResourceConflictException(String message) {
        super(message);
    }

    public ResourceConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
