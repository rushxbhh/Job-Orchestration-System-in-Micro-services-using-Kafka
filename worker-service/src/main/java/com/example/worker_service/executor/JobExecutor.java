package com.example.worker_service.executor;


import com.example.worker_service.dto.ApiResponse;

import com.example.worker_service.enums.ExecutionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JobExecutor {

    public ExecutionResult execute(String jobType){

        log.info("executing the job of type : {}", jobType);

        try{
            return switch (jobType){
                case "DUMMY" -> executeDummy();
                case "HTTP_CALL" -> executeHttpcall();
                default -> ExecutionResult.failure("unknown job type");
            };
        } catch (Exception e) {
            log.error("job execution failed", e);
            return ExecutionResult.failure(e.getMessage());
        }
    }

    private ExecutionResult executeDummy() {
        try {
            log.info("Starting DUMMY job execution...");
            Thread.sleep(2000);  // Simulate work
            log.info("DUMMY job completed successfully");
            return ExecutionResult.success("DUMMY job completed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ExecutionResult.failure("DUMMY job interrupted");
        }
    }

    private ExecutionResult executeHttpcall() {
        try {
            log.info("Starting HTTP_CALL job execution...");
            Thread.sleep(2000);  // Simulate work
            log.info("HTTP_CALL job completed successfully");
            return ExecutionResult.success("HTTP_CALL job completed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ExecutionResult.failure("HTTP_CALL job interrupted");
        }

    }

    @lombok.Value
    public static class ExecutionResult {
        ExecutionStatus status;
        String message;

        public static ExecutionResult success(String message) {
            return new ExecutionResult(ExecutionStatus.SUCCESS, message);
        }

        public static ExecutionResult failure(String message) {
            return new ExecutionResult(ExecutionStatus.FAILED, message);
        }
    }
}
