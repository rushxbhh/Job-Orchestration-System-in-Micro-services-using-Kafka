package com.example.job_service.exception;

public class InvalidJobStateTransitionException extends RuntimeException {
    public InvalidJobStateTransitionException(String message) {
        super(message);
    }
}
