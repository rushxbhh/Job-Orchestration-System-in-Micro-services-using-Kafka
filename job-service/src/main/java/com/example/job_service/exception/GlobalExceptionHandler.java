package com.example.job_service.exception;

import com.example.job_service.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(JobNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(JobAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlreadyFound(JobAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(ex.getMessage()));
    }

//    @ExceptionHandler(InvalidJobStateException.class)
//    public ResponseEntity<ApiResponse<Void>> handleInvalidState(InvalidJobStateException ex) {
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                .body(ApiResponse.failure(ex.getMessage()));
//    }

    @ExceptionHandler(InvalidJobStateTransitionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidJobState(InvalidJobStateTransitionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(JobConcurrentModificationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConcurrentModification(JobConcurrentModificationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(ex.getMessage()));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("Internal server error"));
    }
}

