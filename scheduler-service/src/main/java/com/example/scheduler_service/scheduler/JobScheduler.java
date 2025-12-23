package com.example.scheduler_service.scheduler;

import com.example.scheduler_service.client.JobServiceClient;
import com.example.scheduler_service.dto.ApiResponse;
import com.example.scheduler_service.dto.JobExecutionEvent;
import com.example.scheduler_service.dto.JobResponse;
import com.example.scheduler_service.dto.StatusUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class JobScheduler {

    private final JobServiceClient jobServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.job-execution}")
    private String jobExecutionTopic;

    @Scheduled(fixedDelayString = "${scheduler.scan-interval-ms}")
    public void scanAndSceduleJobs() {
        log.info("=== Starting job scan cycle at {} ===", LocalDateTime.now());
        try{
            int scheduledCount = scheduleCreatedJobs();
            int dispatchedCount = dispatchScheduledJobs();

            log.info("=== Scan completed. Scheduled: {}, Dispatched: {} ===",
                    scheduledCount, dispatchedCount);
        } catch (Exception e) {
            log.error("Error during job scan cycle", e);
        }
    }

    private int scheduleCreatedJobs() {
        try {
            ApiResponse<List<JobResponse>> response = jobServiceClient.getJobsReadyToSchedule();
            if (response.isSuccess() && response.getData() == null) {
                log.warn("Failed to fetch jobs ready to schedule");
                return 0;
            }

            List<JobResponse> jobs = response.getData();
            if (jobs.isEmpty()) {
                log.debug("No jobs ready to schedule");
                return 0;
            }
            log.info("Found {} jobs ready to schedule", jobs.size());
            int successCount = 0;

            for (JobResponse job : jobs) {
                try {
                    StatusUpdateRequest updateRequest = StatusUpdateRequest.builder()
                            .targetStatus("SCHEDULED")
                            .build();

                    jobServiceClient.updateJobStatus(job.getJobId(), updateRequest);
                    log.info("✓ Job {} moved to SCHEDULED status", job.getJobId());
                    successCount++;
                } catch (Exception e) {
                    log.error("✗ Failed to schedule job {}: {}", job.getJobId(), e.getMessage());
                }
            }
            return successCount;
        } catch (Exception e) {
            log.error("Error fetching jobs ready to schedule", e);
            return 0;
        }
    }

    private int dispatchScheduledJobs(){
        try {
            ApiResponse<List<JobResponse>> response = jobServiceClient.getJobsReadyToRun();
            if(!response.isSuccess() && response.getData() == null) {
                log.warn("Failed to fetch jobs ready to run");
                return 0;
            }
                List<JobResponse> jobs = response.getData();

            if (jobs.isEmpty()) {
                log.debug("No jobs ready to dispatch");
                return 0;
            }

            log.info("Found {} jobs ready to dispatch", jobs.size());
            int successCount = 0;

                for( JobResponse job : jobs) {

                    try {
                        StatusUpdateRequest updateRequest = StatusUpdateRequest.builder()
                                .targetStatus("RUNNING")
                                .build();
                        jobServiceClient.updateJobStatus(job.getJobId(), updateRequest);
                        log.info("✓ Job {} marked as RUNNING", job.getJobId());
                        JobExecutionEvent event = JobExecutionEvent.builder()
                                .jobId(job.getJobId())
                                .jobType(job.getJobType().name())
                                .payload(job.getPayload())
                                .scheduledTime(job.getScheduledTime())
                                .dispatchedAt(LocalDateTime.now())
                                .build();

                        kafkaTemplate.send(jobExecutionTopic, job.getJobId().toString(), event);
                        log.info("✓ Job {} dispatched to Kafka topic '{}'",
                                job.getJobId(), jobExecutionTopic);
                        successCount++;
                    } catch (Exception e) {
                        log.error("failed to dispatch the job", job.getJobId(), e.getMessage());
                    }
                }
                    return  successCount;
        } catch (Exception e) {
            log.error("Error fetching jobs ready to run", e);
            return 0;
        }

    }

    public void triggerManualScan() {
        log.info("Manual scan triggered");
        scanAndSceduleJobs();
    }

}
