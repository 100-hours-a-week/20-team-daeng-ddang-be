package com.daengddang.daengdong_map.controller;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.SuccessCode;
import com.daengddang.daengdong_map.controller.api.ExpressionApi;
import com.daengddang.daengdong_map.dto.request.expression.ExpressionAnalyzeRequest;
import com.daengddang.daengdong_map.dto.response.expression.ExpressionAnalyzeResponse;
import com.daengddang.daengdong_map.security.AuthUser;
import com.daengddang.daengdong_map.service.ExpressionAnalyzeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequestMapping("/api/v3/walks/{walkId}/expressions")
@RequiredArgsConstructor
public class ExpressionController implements ExpressionApi {

    private final ExpressionAnalyzeService expressionAnalyzeService;

    @PostMapping("/analysis")
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public ApiResponse<ExpressionAnalyzeResponse> analyze(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long walkId,
            @Valid @RequestBody ExpressionAnalyzeRequest dto
    ) {
        ExpressionAnalyzeResponse response =
                expressionAnalyzeService.analyze(authUser.getUserId(), walkId, dto);
        return ApiResponse.success(SuccessCode.EMOTION_ANALYSIS_RESULT_CREATED, response);
    }

    @GetMapping("/analysis")
    @Override
    public ApiResponse<ExpressionAnalyzeResponse> getAnalysis(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long walkId
    ) {
        ExpressionAnalyzeResponse response =
                expressionAnalyzeService.getAnalysis(authUser.getUserId(), walkId);
        return ApiResponse.success(SuccessCode.EMOTION_ANALYSIS_RESULT_RETRIEVED, response);
    }
}
