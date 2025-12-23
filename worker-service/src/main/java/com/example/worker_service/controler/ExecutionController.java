package com.example.worker_service.controler;

import com.example.worker_service.entity.JobExecution;
import com.example.worker_service.repository.JobExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/executions")
@RequiredArgsConstructor
public class ExecutionController {

    private final JobExecutionRepository executionRepository;

    @GetMapping
    public ResponseEntity<List<JobExecution>> getAllExecutions() {
        return ResponseEntity.ok(executionRepository.findAll());
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<JobExecution>> getExecutionsByJobId(@PathVariable UUID jobId) {
        return ResponseEntity.ok(executionRepository.findByJobIdOrderByStartTimeDesc(jobId));
    }

    @GetMapping("/{executionId}")
    public ResponseEntity<JobExecution> getExecutionById(@PathVariable UUID executionId) {
        return executionRepository.findById(executionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
