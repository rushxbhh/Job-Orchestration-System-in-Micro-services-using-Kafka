package com.example.job_service.exception;

public class JobConcurrentModificationException extends RuntimeException {
    public JobConcurrentModificationException(String message) {
        super(message);
    }
}
