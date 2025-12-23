package com.example.worker_service.client;

import com.example.worker_service.dto.ApiResponse;
import com.example.worker_service.dto.JobResponse;
import com.example.worker_service.dto.StatusUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "job-service", url = "${worker:job-service-url}")
public interface JobServiceClient {

    @GetMapping("/api/v1/jobs/{jobId}")
    ApiResponse<JobResponse> getJob(@PathVariable("jobId") UUID jobId);

    @PutMapping("/api/v1/jobs/{jobId}")
    ApiResponse<JobResponse> updateJobStatus(
            @PathVariable UUID jobId,
            @RequestBody StatusUpdateRequest request
    );
}
