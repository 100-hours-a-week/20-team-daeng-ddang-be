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
import com.daengddang.daengdong_map.sse.AnalysisTaskSseService;
import com.daengddang.daengdong_map.sse.AsyncSseProperties;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v3")
@RequiredArgsConstructor
public class AnalysisTaskController implements AnalysisTaskApi {

    private final ExternalAnalysisTaskService externalAnalysisTaskService;
    private final AsyncSseProperties asyncSseProperties;
    private final AnalysisTaskSseService analysisTaskSseService;

    @PostMapping("/walks/{walkId}/missions/analysis/tasks")
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

    @PostMapping("/walks/{walkId}/expressions/analysis/tasks")
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

    @PostMapping("/healthcares/analysis/tasks")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Override
    public ApiResponse<AnalysisTaskAcceptedResponse> createHealthcareTask(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody HealthcareAnalyzeRequest dto
    ) {
        AnalysisTaskAcceptedResponse response =
                externalAnalysisTaskService.createHealthcareTask(authUser.getUserId(), dto);
        return ApiResponse.success(SuccessCode.ANALYSIS_TASK_ACCEPTED, response);
    }

    @GetMapping("/walks/{walkId}/analysis/tasks/{taskId}")
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

    @GetMapping("/healthcares/analysis/tasks/{taskId}")
    @Override
    public ApiResponse<AnalysisTaskDetailResponse> getHealthcareTask(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable String taskId
    ) {
        AnalysisTaskDetailResponse response =
                externalAnalysisTaskService.getHealthcareTask(authUser.getUserId(), taskId);
        return ApiResponse.success(SuccessCode.ANALYSIS_TASK_RETRIEVED, response);
    }

    @GetMapping(value = "/walks/{walkId}/analysis/tasks/{taskId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Override
    public SseEmitter subscribeTaskEvents(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long walkId,
            @PathVariable String taskId
    ) {
        AnalysisTaskDetailResponse response =
                externalAnalysisTaskService.getTask(authUser.getUserId(), walkId, taskId);
        return analysisTaskSseService.subscribe(response, asyncSseProperties.getEmitterTimeoutMs());
    }

    @GetMapping(value = "/healthcares/analysis/tasks/{taskId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Override
    public SseEmitter subscribeHealthcareTaskEvents(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable String taskId
    ) {
        AnalysisTaskDetailResponse response =
                externalAnalysisTaskService.getHealthcareTask(authUser.getUserId(), taskId);
        return analysisTaskSseService.subscribe(response, asyncSseProperties.getEmitterTimeoutMs());
    }
}
