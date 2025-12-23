package com.example.job_service.service;


import com.example.job_service.domain.JobTransitionValidator;
import com.example.job_service.dto.CreateJobRequest;
import com.example.job_service.dto.ExecutionResponse;
import com.example.job_service.dto.JobResponse;
import com.example.job_service.dto.StatusUpdateRequest;
import com.example.job_service.entity.Job;
import com.example.job_service.enums.JobStatus;
import com.example.job_service.exception.JobAlreadyExistsException;
import com.example.job_service.exception.JobConcurrentModificationException;
import com.example.job_service.exception.JobNotFoundException;
import com.example.job_service.repository.JobExecutionRepository;
import com.example.job_service.repository.JobRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final JobExecutionRepository executionRepository;
    private final ModelMapper modelMapper;
    private final JobTransitionValidator jobTransitionValidator;

    public JobResponse createJob(CreateJobRequest request) {

        if (jobRepository.existsByName(request.getName())) {
            throw new JobAlreadyExistsException("Job with name '" + request.getName() + "' already exists");
        }

        Job job = Job.builder()
                .name(request.getName())
                .jobType(request.getJobType())
                .payload(request.getPayload())
                .scheduledTime(request.getScheduledTime())
                .status(JobStatus.CREATED)
                .build();

        Job savedJob = jobRepository.save(job);
        return mapToResponse(savedJob);
    }

    public JobResponse updateJobStatus(UUID jobId, StatusUpdateRequest request) {
        try {
            Job job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new JobNotFoundException("Job not found: " + jobId));

            JobStatus currentStatus = job.getStatus();
            JobStatus targetStatus = request.getTargetStatus();

            // Validate state transition
            jobTransitionValidator.validate(currentStatus, targetStatus);

            // Update status
            job.setStatus(targetStatus);

            Job savedJob = jobRepository.save(job);
            return mapToResponse(savedJob);

        } catch (OptimisticLockException e) {
            throw new JobConcurrentModificationException(
                    "Job was modified by another process: " + jobId + ". Please retry."
            );
        }
    }

    public JobResponse getJob(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException("Job not found: " + jobId));

        return modelMapper.map(job, JobResponse.class);
    }

    public List<ExecutionResponse> getExecutions(UUID jobId) {
        return executionRepository.findByJobId(jobId).stream()
                .map(ex -> modelMapper.map(ex, ExecutionResponse.class))
                .toList();
    }

    public List<JobResponse> findJobsReadyToSchedule() {
        List<Job> jobs = jobRepository.findByStatusAndScheduledTimeLessThanEqual(
                JobStatus.CREATED,
                LocalDateTime.now()
        );

        return jobs.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<JobResponse> findJobsReadyToRun() {
        List<Job> jobs = jobRepository.findByStatusAndScheduledTimeLessThanEqual(
                JobStatus.SCHEDULED,
                LocalDateTime.now()
        );

        return jobs.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private JobResponse mapToResponse(Job job) {
        return JobResponse.builder()
                .jobId(job.getId())
                .name(job.getName())
                .jobType(job.getJobType())
                .status(job.getStatus())
                .payload(job.getPayload())
                .scheduledTime(job.getScheduledTime())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .version(job.getVersion())
                .build();
    }
}

