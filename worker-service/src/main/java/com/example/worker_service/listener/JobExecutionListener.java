package com.example.worker_service.listener;

import com.example.worker_service.client.JobServiceClient;
import com.example.worker_service.dto.ApiResponse;
import com.example.worker_service.dto.JobExecutionEvent;
import com.example.worker_service.dto.StatusUpdateRequest;
import com.example.worker_service.entity.JobExecution;
import com.example.worker_service.enums.ExecutionStatus;
import com.example.worker_service.executor.JobExecutor;
import com.example.worker_service.repository.JobExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Component
@Slf4j
@RequiredArgsConstructor
public class JobExecutionListener {

    private final JobServiceClient jobServiceClient;
    private final JobExecutor jobExecutor;
    private final JobExecutionRepository jobExecutionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(
            topics = "${kafka.topics.job-execution}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleJobExecution(String message, Acknowledgment acknowledgment) {
        log.info("received kafka msg: {} ", message);

        JobExecution execution = null;
        JobExecutionEvent event = null;

        try {
            event = objectMapper.readValue(message, JobExecutionEvent.class);
            log.info("job is processing : {}", event.getJobId());

            Optional<JobExecution> lastExecution = jobExecutionRepository
                    .findFirstByJobIdOrderByStartTimeDesc(event.getJobId());

            if(lastExecution.isPresent()) {
                JobExecution last = lastExecution.get();

                if (last.getStatus() == ExecutionStatus.RUNNING){
                    log.warn( "job {} is already running, skip duplicate", event.getJobId());
                    acknowledgment.acknowledge();
                    return;
                }

                if (last.getStatus() == ExecutionStatus.SUCCESS){
                    log.warn( "job {} is already completed successfully, skip duplicate", event.getJobId());
                    acknowledgment.acknowledge();
                    return;
                }
                log.info("job failed {}, attempting retry", event.getJobId());
            }

            try{
                updateJobStatus(event.getJobId(), "RUNNING");
                log.info(" job {} status updated to running" , event.getJobId());

            } catch (Exception e) {
                log.error("failed to update job status to running : {} ", e.getMessage());
            }

            execution = JobExecution.builder()
                    .jobId(event.getJobId())
                    .status(ExecutionStatus.RUNNING)
                    .startTime(LocalDateTime.now())
                    .attemptNumber(calculateAttemptNumber(event.getJobId()))
                    .build();

            execution = jobExecutionRepository.save(execution);
            log.info("created job record: {}", execution.getJobId());

            log.info("executing job");
            JobExecutor.ExecutionResult result = jobExecutor.execute(event.getJobType());

            execution.setStatus(result.getStatus());
            execution.setEndTime(LocalDateTime.now());

            if (result.getStatus() == ExecutionStatus.SUCCESS) {
                execution.setResult(result.getMessage());
                log.info("Job {} completed with SUCCESS", event.getJobId());
            } else {
                execution.setErrorMessage(result.getMessage());
                log.error("Job {} completed with FAILED", event.getJobId());
            }

            jobExecutionRepository.save(execution);

            String finalStatus = result.getStatus() == ExecutionStatus.SUCCESS ? "SUCCESS" : "FAILED";
            updateJobStatus(event.getJobId(), finalStatus);
            log.info("✓ Job {} final status: {}", event.getJobId(), finalStatus);

            acknowledgment.acknowledge();
            log.info("✓ Message acknowledged for job {}", event.getJobId());

        } catch (Exception e) {
            log.error(" Error processing job {}",
                    event != null ? event.getJobId() : "unknown", e);

            if (execution != null) {
                try {
                    execution.setStatus(ExecutionStatus.FAILED);
                    execution.setEndTime(LocalDateTime.now());
                    execution.setErrorMessage("Worker error: " + e.getMessage());
                    jobExecutionRepository.save(execution);

                    if (event != null) {
                        updateJobStatus(event.getJobId(), "FAILED");
                    }
                } catch (Exception ex) {
                    log.error("Failed to update error status", ex);
                }
            }
        }
        log.warn("Message NOT acknowledged. Will retry...");
    }

    private int calculateAttemptNumber(UUID jobId){
        return (int) jobExecutionRepository.findByJobId(jobId).stream()
                .filter(e -> e.getStatus() == ExecutionStatus.FAILED)
                .count() + 1;
    }

    private void updateJobStatus(UUID jobId, String status) {
        try{
            StatusUpdateRequest request = StatusUpdateRequest.builder()
                    .targetStatus(status)
                    .build();

            jobServiceClient.updateJobStatus(jobId, request);
            log.debug("update job {} status to {}", jobId, status);

        } catch (Exception e) {
            log.error("failed to update job {} with status {} : {}", jobId, status, e.getMessage());
            throw new RuntimeException("failed to update the job");
        }
    }
}
