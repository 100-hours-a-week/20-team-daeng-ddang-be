package com.daengddang.daengdong_map.controller.api;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.api.ErrorCodes;
import com.daengddang.daengdong_map.dto.request.mission.MissionUploadRequest;
import com.daengddang.daengdong_map.dto.response.mission.MissionJudgeResponse;
import com.daengddang.daengdong_map.dto.response.mission.MissionUploadListResponse;
import com.daengddang.daengdong_map.dto.response.mission.MissionUploadResponse;
import com.daengddang.daengdong_map.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Mission", description = "Mission analysis and upload endpoints")
public interface MissionApi {

    @Operation(summary = "Analyze mission uploads")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INVALID_FORMAT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RESOURCE_NOT_FOUND"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "AI_SERVER_CONNECTION_FAILED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_FORMAT,
            com.daengddang.daengdong_map.common.ErrorCode.RESOURCE_NOT_FOUND,
            com.daengddang.daengdong_map.common.ErrorCode.AI_SERVER_CONNECTION_FAILED
    })
    ApiResponse<MissionJudgeResponse> judgeMissions(
            @Parameter(hidden = true) AuthUser authUser,
            @PathVariable Long walkId
    );

    @Operation(summary = "Upload mission video")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INVALID_FORMAT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RESOURCE_NOT_FOUND"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "WALK_ALREADY_ENDED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_FORMAT,
            com.daengddang.daengdong_map.common.ErrorCode.RESOURCE_NOT_FOUND,
            com.daengddang.daengdong_map.common.ErrorCode.WALK_ALREADY_ENDED
    })
    ApiResponse<MissionUploadResponse> saveUpload(
            @Parameter(hidden = true) AuthUser authUser,
            @PathVariable Long walkId,
            @RequestBody MissionUploadRequest dto
    );

    @Operation(summary = "Get mission uploads")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RESOURCE_NOT_FOUND"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.RESOURCE_NOT_FOUND
    })
    ApiResponse<MissionUploadListResponse> getUploads(
            @Parameter(hidden = true) AuthUser authUser,
            @PathVariable Long walkId
    );
}
