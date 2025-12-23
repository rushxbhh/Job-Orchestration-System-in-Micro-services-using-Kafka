package com.example.job_service.domain;


import com.example.job_service.enums.JobStatus;
import com.example.job_service.exception.InvalidJobStateTransitionException;
import org.springframework.stereotype.Component;

import static com.example.job_service.enums.JobStatus.*;

@Component
public class JobTransitionValidator {

    public void validate(JobStatus current, JobStatus target) {
        if (current == target) return;

        switch (current) {
            case CREATED -> {
                if (target != SCHEDULED) {
                    throwInvalidTransition(current, target);
                }
            }
            case SCHEDULED -> {
                if (target != RUNNING) {
                    throwInvalidTransition(current, target);
                }
            }
            case RUNNING -> {
                if (target != JobStatus.SUCCESS && target != JobStatus.FAILED) {
                    throwInvalidTransition(current, target);
                }
            }
            case FAILED -> {
                // Allow retry by rescheduling
                if (target != SCHEDULED) {
                    throwInvalidTransition(current, target);
                }
            }
            case SUCCESS -> {
                // Terminal state - cannot transition from SUCCESS
                throwInvalidTransition(current, target);
            }
            default -> throwInvalidTransition(current, target);
        }
    }

    private void throwInvalidTransition(JobStatus current, JobStatus target) {
        throw new InvalidJobStateTransitionException(
                String.format("Invalid state transition from %s to %s", current, target)
        );
    }
}
