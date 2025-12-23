package com.example.scheduler_service.dto;

import com.example.scheduler_service.enums.JobStatus;
import lombok.*;

import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatusUpdateRequest {

  //  @NotNull
    private String targetStatus;

}

