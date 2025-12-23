package com.example.worker_service.repository;

import com.example.worker_service.entity.JobExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobExecutionRepository extends JpaRepository<JobExecution, UUID> {


    List<JobExecution> findByJobId(UUID jobId);
    List<JobExecution> findByJobIdOrderByStartTimeDesc(UUID jobId);

    // For idempotency tocheck if job already has a running/success execution
    Optional<JobExecution> findFirstByJobIdOrderByStartTimeDesc(UUID jobId);
}
