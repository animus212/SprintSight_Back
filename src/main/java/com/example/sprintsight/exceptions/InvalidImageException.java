package com.example.sprintsight.exceptions;

import java.io.Serial;

public class InvalidImageException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidImageException(String message) {
        super(message);
    }

    public InvalidImageException(String message, Throwable cause) {
        super(message, cause);
    }
}
