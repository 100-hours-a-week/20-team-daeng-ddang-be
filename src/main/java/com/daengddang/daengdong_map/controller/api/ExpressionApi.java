package com.daengddang.daengdong_map.controller.api;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.api.ErrorCodes;
import com.daengddang.daengdong_map.dto.request.expression.ExpressionAnalyzeRequest;
import com.daengddang.daengdong_map.dto.response.expression.ExpressionAnalyzeResponse;
import com.daengddang.daengdong_map.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Expression", description = "Emotion analysis endpoints")
public interface ExpressionApi {

    @Operation(summary = "Analyze dog expression")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INVALID_FORMAT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RESOURCE_NOT_FOUND"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "EMOTION_ANALYSIS_ALREADY_RUNNING"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "DOG_FACE_NOT_RECOGNIZED, CHAT_ANALYSIS_NOT_SUPPORTED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "AI_SERVER_CONNECTION_FAILED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_FORMAT,
            com.daengddang.daengdong_map.common.ErrorCode.RESOURCE_NOT_FOUND,
            com.daengddang.daengdong_map.common.ErrorCode.EMOTION_ANALYSIS_ALREADY_RUNNING,
            com.daengddang.daengdong_map.common.ErrorCode.DOG_FACE_NOT_RECOGNIZED,
            com.daengddang.daengdong_map.common.ErrorCode.CHAT_ANALYSIS_NOT_SUPPORTED,
            com.daengddang.daengdong_map.common.ErrorCode.AI_SERVER_CONNECTION_FAILED
    })
    ApiResponse<ExpressionAnalyzeResponse> analyze(
            @Parameter(hidden = true) AuthUser authUser,
            @PathVariable Long walkId,
            @RequestBody ExpressionAnalyzeRequest dto
    );
}
