package com.daengddang.daengdong_map.controller.api;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.api.ErrorCodes;
import com.daengddang.daengdong_map.dto.request.diaries.WalkDiariesCreateRequest;
import com.daengddang.daengdong_map.dto.request.walk.WalkEndRequest;
import com.daengddang.daengdong_map.dto.request.walk.WalkStartRequest;
import com.daengddang.daengdong_map.dto.response.diaries.WalkDiariesCreateResponse;
import com.daengddang.daengdong_map.dto.response.walk.OccupiedBlockListResponse;
import com.daengddang.daengdong_map.dto.response.walk.WalkEndResponse;
import com.daengddang.daengdong_map.dto.response.walk.WalkStartResponse;
import com.daengddang.daengdong_map.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Walk", description = "Walk lifecycle endpoints")
public interface WalkApi {

    @Operation(summary = "Start walk")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INVALID_FORMAT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RESOURCE_NOT_FOUND"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "WALK_ALREADY_IN_PROGRESS"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_FORMAT,
            com.daengddang.daengdong_map.common.ErrorCode.RESOURCE_NOT_FOUND,
            com.daengddang.daengdong_map.common.ErrorCode.WALK_ALREADY_IN_PROGRESS
    })
    ApiResponse<WalkStartResponse> startWalk(
            @Parameter(hidden = true) AuthUser authUser,
            @RequestBody WalkStartRequest dto
    );

    @Operation(summary = "End walk")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INVALID_FORMAT, INVALID_WALK_METRICS"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RESOURCE_NOT_FOUND"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "WALK_ALREADY_ENDED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_FORMAT,
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_WALK_METRICS,
            com.daengddang.daengdong_map.common.ErrorCode.RESOURCE_NOT_FOUND,
            com.daengddang.daengdong_map.common.ErrorCode.WALK_ALREADY_ENDED
    })
    ApiResponse<WalkEndResponse> endWalk(
            @Parameter(hidden = true) AuthUser authUser,
            @PathVariable Long walkId,
            @RequestBody WalkEndRequest dto
    );

    @Operation(summary = "Get occupied blocks in a walk")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RESOURCE_NOT_FOUND"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.RESOURCE_NOT_FOUND
    })
    ApiResponse<OccupiedBlockListResponse> getOccupiedBlocks(
            @Parameter(hidden = true) AuthUser authUser,
            @PathVariable Long walkId
    );

    @Operation(summary = "Create walk diary")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "WALK_RECORD_NOT_FOUND"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.WALK_RECORD_NOT_FOUND
    })
    ApiResponse<WalkDiariesCreateResponse> writeWalkDiary(
            @Parameter(hidden = true) AuthUser authUser,
            @PathVariable Long walkId,
            @RequestBody WalkDiariesCreateRequest dto
    );
}
