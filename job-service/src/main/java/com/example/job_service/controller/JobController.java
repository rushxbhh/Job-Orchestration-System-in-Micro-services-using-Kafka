package com.example.job_service.controller;

import com.example.job_service.dto.*;
import com.example.job_service.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ApiResponse<JobResponse> createJob(
            @Valid @RequestBody CreateJobRequest request) {

        return ApiResponse.success(
                "Job created",
                jobService.createJob(request)
        );
    }

    @GetMapping("/{jobId}")
    public ApiResponse<JobResponse> getJob(@PathVariable UUID jobId) {
        return ApiResponse.success(
                "Job fetched",
                jobService.getJob(jobId)
        );
    }

    @GetMapping("/{jobId}/executions")
    public ApiResponse<List<ExecutionResponse>> getExecutions(@PathVariable UUID jobId) {
        return ApiResponse.success(
                "Executions fetched",
                jobService.getExecutions(jobId)
        );
    }

    @PutMapping("/{jobId}")
    public ApiResponse<Void> updateJobStatus(
            @PathVariable UUID jobId,
            @RequestBody StatusUpdateRequest request) {

        jobService.updateJobStatus(jobId, request);
        return ApiResponse.success("Job state updated", null);
    }

    @GetMapping("/ready-to-schedule")
    public ApiResponse<List<JobResponse>> getJobsReadyToSchedule() {
        log.info("Received request to get jobs ready to schedule");
        return ApiResponse.success(
                "Jobs ready to schedule",
                jobService.findJobsReadyToSchedule()
        );
    }

    @GetMapping("/ready-to-run")
    public ApiResponse<List<JobResponse>> getJobsReadyToRun() {
        log.info("Received request to get jobs ready to run");
        return ApiResponse.success(
                "Jobs ready to run",
                jobService.findJobsReadyToRun()
        );
    }
}
