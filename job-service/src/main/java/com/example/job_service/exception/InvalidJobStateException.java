package com.example.job_service.exception;

public class InvalidJobStateException extends RuntimeException {

    public InvalidJobStateException(String message)
    {
        super(message);
    }
}
