package com.example.job_service.exception;

public class JobAlreadyExistsException extends RuntimeException {
    public JobAlreadyExistsException(String message) {
        super(message);
    }
}
