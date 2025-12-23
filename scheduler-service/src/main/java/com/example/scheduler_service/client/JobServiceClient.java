package com.example.scheduler_service.client;


import com.example.scheduler_service.dto.ApiResponse;
import com.example.scheduler_service.dto.JobResponse;
import com.example.scheduler_service.dto.StatusUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "job-service", url = "${scheduler.job-service.url}")
public interface JobServiceClient {

    @GetMapping("/api/v1/jobs/ready-to-schedule")
    ApiResponse<List<JobResponse>> getJobsReadyToSchedule();

    @GetMapping("/api/v1/jobs/ready-to-run")
    ApiResponse<List<JobResponse>> getJobsReadyToRun();

    @PutMapping("/api/v1/jobs/{jobId}")
    ApiResponse<JobResponse> updateJobStatus(
            @PathVariable UUID jobId,
            @RequestBody StatusUpdateRequest request
    );
}
