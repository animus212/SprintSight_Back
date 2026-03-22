package com.example.SprintSight.Exceptions;

import java.io.Serial;

public class UsernameAlreadyExistsException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public UsernameAlreadyExistsException(String message) {
        super(message);
    }
}
