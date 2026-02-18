package com.daengddang.daengdong_map.controller.api;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.api.ErrorCodes;
import com.daengddang.daengdong_map.dto.request.expression.ExpressionAnalyzeRequest;
import com.daengddang.daengdong_map.dto.request.healthcare.HealthcareAnalyzeRequest;
import com.daengddang.daengdong_map.dto.response.task.AnalysisTaskAcceptedResponse;
import com.daengddang.daengdong_map.dto.response.task.AnalysisTaskDetailResponse;
import com.daengddang.daengdong_map.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Analysis Task", description = "Async analysis task endpoints")
public interface AnalysisTaskApi {

    @Operation(summary = "Create mission analysis task")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Accepted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "FORBIDDEN"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RESOURCE_NOT_FOUND"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INVALID_FORMAT")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_FORMAT,
            com.daengddang.daengdong_map.common.ErrorCode.FORBIDDEN,
            com.daengddang.daengdong_map.common.ErrorCode.RESOURCE_NOT_FOUND
    })
    ApiResponse<AnalysisTaskAcceptedResponse> createMissionTask(
            @Parameter(hidden = true) AuthUser authUser,
            @PathVariable Long walkId
    );

    @Operation(summary = "Create expression analysis task")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Accepted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INVALID_FORMAT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "FORBIDDEN"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RESOURCE_NOT_FOUND")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_FORMAT,
            com.daengddang.daengdong_map.common.ErrorCode.FORBIDDEN,
            com.daengddang.daengdong_map.common.ErrorCode.RESOURCE_NOT_FOUND
    })
    ApiResponse<AnalysisTaskAcceptedResponse> createExpressionTask(
            @Parameter(hidden = true) AuthUser authUser,
            @PathVariable Long walkId,
            @RequestBody ExpressionAnalyzeRequest dto
    );

    @Operation(summary = "Create healthcare analysis task")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Accepted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INVALID_FORMAT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "FORBIDDEN"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RESOURCE_NOT_FOUND")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_FORMAT,
            com.daengddang.daengdong_map.common.ErrorCode.FORBIDDEN,
            com.daengddang.daengdong_map.common.ErrorCode.RESOURCE_NOT_FOUND
    })
    ApiResponse<AnalysisTaskAcceptedResponse> createHealthcareTask(
            @Parameter(hidden = true) AuthUser authUser,
            @PathVariable Long walkId,
            @RequestBody HealthcareAnalyzeRequest dto
    );

    @Operation(summary = "Get analysis task")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "FORBIDDEN"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RESOURCE_NOT_FOUND")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.FORBIDDEN,
            com.daengddang.daengdong_map.common.ErrorCode.RESOURCE_NOT_FOUND
    })
    ApiResponse<AnalysisTaskDetailResponse> getTask(
            @Parameter(hidden = true) AuthUser authUser,
            @PathVariable Long walkId,
            @PathVariable String taskId
    );
}
