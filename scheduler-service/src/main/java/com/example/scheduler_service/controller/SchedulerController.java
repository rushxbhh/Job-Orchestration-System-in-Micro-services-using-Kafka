package com.example.scheduler_service.controller;

import com.example.scheduler_service.dto.ApiResponse;
import com.example.scheduler_service.scheduler.JobScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/scheduler")
@RequiredArgsConstructor
public class SchedulerController {

    private final JobScheduler jobScheduler;

    @PostMapping("/scan")
    public ApiResponse<Map<String, String>> triggerScan(){
        try {
            jobScheduler.triggerManualScan();
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Manual scan triggered successfully");
            return ApiResponse.success("success", response);
        }
        catch (Exception e)
        {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to trigger scan: " + e.getMessage());
            return ApiResponse.failure("failure", response);
        }
    }
}
