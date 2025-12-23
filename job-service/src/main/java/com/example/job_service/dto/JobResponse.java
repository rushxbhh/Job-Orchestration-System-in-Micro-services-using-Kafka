package com.example.job_service.dto;

import com.example.job_service.enums.JobStatus;
import com.example.job_service.enums.JobType;
import lombok.*;

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


