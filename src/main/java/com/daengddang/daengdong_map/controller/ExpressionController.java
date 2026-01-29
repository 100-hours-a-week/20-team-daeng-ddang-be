package com.daengddang.daengdong_map.controller;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.SuccessCode;
import com.daengddang.daengdong_map.dto.request.expression.FaceAnalyzeRequest;
import com.daengddang.daengdong_map.dto.response.expression.FaceAnalyzeResponse;
import com.daengddang.daengdong_map.security.AuthUser;
import com.daengddang.daengdong_map.service.FaceAnalyzeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/v3/walks/{walkId}/expressions")
@RequiredArgsConstructor
public class ExpressionController {

    private final FaceAnalyzeService faceAnalyzeService;

    @PostMapping("/analysis")
    public ApiResponse<FaceAnalyzeResponse> analyze(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long walkId,
            @Valid @RequestBody FaceAnalyzeRequest request
    ) {
        log.info("표정분석 실행");
        FaceAnalyzeResponse response =
                faceAnalyzeService.analyze(authUser.getUserId(), walkId, request);
        return ApiResponse.success(SuccessCode.EMOTION_ANALYSIS_RESULT_CREATED, response);
    }
}
