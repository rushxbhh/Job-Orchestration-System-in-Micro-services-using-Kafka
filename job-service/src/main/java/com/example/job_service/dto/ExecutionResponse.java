package com.example.job_service.dto;

import com.example.job_service.enums.JobStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ExecutionResponse {

    private UUID executionId;
    private JobStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String errorMessage;
}

