package com.example.sprintsight.exceptions;

import java.io.Serial;

public class BusinessRuleViolationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public BusinessRuleViolationException(String message) {
        super(message);
    }

    public BusinessRuleViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
