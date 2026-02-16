package com.daengddang.daengdong_map.controller.api;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.api.ErrorCodes;
import com.daengddang.daengdong_map.dto.request.healthcare.HealthcareAnalyzeRequest;
import com.daengddang.daengdong_map.dto.response.healthcare.HealthcareAnalyzeResponse;
import com.daengddang.daengdong_map.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Healthcare", description = "Healthcare analysis endpoints")
public interface HealthcareApi {

    @Operation(summary = "Analyze healthcare from dog video")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INVALID_FORMAT, INVALID_FILE_URL, VIDEO_LENGTH_EXCEEDED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "FORBIDDEN"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RESOURCE_NOT_FOUND"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "415", description = "FILE_TYPE_UNSUPPORTED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "DOG_FACE_NOT_RECOGNIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "AI_SERVER_CONNECTION_FAILED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_FORMAT,
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_FILE_URL,
            com.daengddang.daengdong_map.common.ErrorCode.VIDEO_LENGTH_EXCEEDED,
            com.daengddang.daengdong_map.common.ErrorCode.FILE_TYPE_UNSUPPORTED,
            com.daengddang.daengdong_map.common.ErrorCode.FORBIDDEN,
            com.daengddang.daengdong_map.common.ErrorCode.RESOURCE_NOT_FOUND,
            com.daengddang.daengdong_map.common.ErrorCode.DOG_FACE_NOT_RECOGNIZED,
            com.daengddang.daengdong_map.common.ErrorCode.AI_SERVER_CONNECTION_FAILED
    })
    ApiResponse<HealthcareAnalyzeResponse> analyze(
            @Parameter(hidden = true) AuthUser authUser,
            @RequestBody HealthcareAnalyzeRequest request
    );

    @Operation(summary = "Get healthcare analysis detail")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INVALID_FORMAT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "FORBIDDEN"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RESOURCE_NOT_FOUND"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_FORMAT,
            com.daengddang.daengdong_map.common.ErrorCode.FORBIDDEN,
            com.daengddang.daengdong_map.common.ErrorCode.RESOURCE_NOT_FOUND
    })
    ApiResponse<HealthcareAnalyzeResponse> getHealthcare(
            @Parameter(hidden = true) AuthUser authUser,
            @PathVariable Long healthcareId
    );
}
