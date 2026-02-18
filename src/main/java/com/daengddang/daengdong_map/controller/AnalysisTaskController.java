package com.daengddang.daengdong_map.controller;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.SuccessCode;
import com.daengddang.daengdong_map.controller.api.AnalysisTaskApi;
import com.daengddang.daengdong_map.dto.request.expression.ExpressionAnalyzeRequest;
import com.daengddang.daengdong_map.dto.request.healthcare.HealthcareAnalyzeRequest;
import com.daengddang.daengdong_map.dto.response.task.AnalysisTaskAcceptedResponse;
import com.daengddang.daengdong_map.dto.response.task.AnalysisTaskDetailResponse;
import com.daengddang.daengdong_map.security.AuthUser;
import com.daengddang.daengdong_map.service.ExternalAnalysisTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v3/walks/{walkId}")
@RequiredArgsConstructor
public class AnalysisTaskController implements AnalysisTaskApi {

    private final ExternalAnalysisTaskService externalAnalysisTaskService;

    @PostMapping("/missions/analysis-tasks")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Override
    public ApiResponse<AnalysisTaskAcceptedResponse> createMissionTask(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long walkId
    ) {
        AnalysisTaskAcceptedResponse response =
                externalAnalysisTaskService.createMissionTask(authUser.getUserId(), walkId);
        return ApiResponse.success(SuccessCode.ANALYSIS_TASK_ACCEPTED, response);
    }

    @PostMapping("/expressions/analysis-tasks")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Override
    public ApiResponse<AnalysisTaskAcceptedResponse> createExpressionTask(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long walkId,
            @Valid @RequestBody ExpressionAnalyzeRequest dto
    ) {
        AnalysisTaskAcceptedResponse response =
                externalAnalysisTaskService.createExpressionTask(authUser.getUserId(), walkId, dto);
        return ApiResponse.success(SuccessCode.ANALYSIS_TASK_ACCEPTED, response);
    }

    @PostMapping("/healthcare/analysis-tasks")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Override
    public ApiResponse<AnalysisTaskAcceptedResponse> createHealthcareTask(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long walkId,
            @Valid @RequestBody HealthcareAnalyzeRequest dto
    ) {
        AnalysisTaskAcceptedResponse response =
                externalAnalysisTaskService.createHealthcareTask(authUser.getUserId(), walkId, dto);
        return ApiResponse.success(SuccessCode.ANALYSIS_TASK_ACCEPTED, response);
    }

    @GetMapping("/analysis-tasks/{taskId}")
    @Override
    public ApiResponse<AnalysisTaskDetailResponse> getTask(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long walkId,
            @PathVariable String taskId
    ) {
        AnalysisTaskDetailResponse response =
                externalAnalysisTaskService.getTask(authUser.getUserId(), walkId, taskId);
        return ApiResponse.success(SuccessCode.ANALYSIS_TASK_RETRIEVED, response);
    }
}
