package com.daengddang.daengdong_map.controller;

import com.daengddang.daengdong_map.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v3")
public class HealthCheckController {

    @GetMapping("/health")
    public ApiResponse<Void> getHealthCheck() {
        return ApiResponse.success("success");
    }
}
