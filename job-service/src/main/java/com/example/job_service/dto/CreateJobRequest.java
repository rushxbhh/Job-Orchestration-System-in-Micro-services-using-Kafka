package com.example.job_service.dto;

import com.example.job_service.enums.JobType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateJobRequest {

    @NotBlank
    private String name;

    @NotNull
    @Future
    private LocalDateTime scheduledTime;

    @NotNull
    private JobType jobType;

   // private Long version;

    private String payload;
}

