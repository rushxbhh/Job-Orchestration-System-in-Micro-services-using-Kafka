package com.example.job_service.dto;

import com.example.job_service.enums.JobStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class StatusUpdateRequest {

    @NotNull
    private JobStatus targetStatus;

}

