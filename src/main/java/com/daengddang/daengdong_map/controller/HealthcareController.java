package com.daengddang.daengdong_map.controller;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.SuccessCode;
import com.daengddang.daengdong_map.controller.api.HealthcareApi;
import com.daengddang.daengdong_map.dto.request.healthcare.HealthcareAnalyzeRequest;
import com.daengddang.daengdong_map.dto.response.healthcare.HealthcareAnalyzeResponse;
import com.daengddang.daengdong_map.security.AuthUser;
import com.daengddang.daengdong_map.service.HealthcareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v3/healthcares")
@RequiredArgsConstructor
public class HealthcareController implements HealthcareApi {

    private final HealthcareService healthcareService;

    @PostMapping
    @Override
    public ApiResponse<HealthcareAnalyzeResponse> analyze(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody HealthcareAnalyzeRequest dto
    ) {
        HealthcareAnalyzeResponse response = healthcareService.analyze(authUser.getUserId(), dto);
        return ApiResponse.success(SuccessCode.HEALTHCARE_ANALYSIS_RESULT_RETRIEVED, response);
    }

    @GetMapping("/{healthcareId}")
    @Override
    public ApiResponse<HealthcareAnalyzeResponse> getHealthcare(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long healthcareId
    ) {
        HealthcareAnalyzeResponse response = healthcareService.getHealthcare(authUser.getUserId(), healthcareId);
        return ApiResponse.success(SuccessCode.HEALTHCARE_ANALYSIS_RESULT_RETRIEVED, response);
    }
}
