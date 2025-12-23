package com.example.worker_service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobExecutionEvent {

    private UUID jobId;
    private String jobType;
    private String payload;
    private LocalDateTime scheduledTime;
    private LocalDateTime dispatchedAt;

}
