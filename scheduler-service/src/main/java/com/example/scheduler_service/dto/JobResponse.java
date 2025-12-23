package com.example.scheduler_service.dto;

import com.example.scheduler_service.enums.JobStatus;
import com.example.scheduler_service.enums.JobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobResponse {

    private UUID jobId;
    private String name;
    private JobStatus status;
    private JobType jobType;
    private String payload;
    private Long version;
    private LocalDateTime scheduledTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


