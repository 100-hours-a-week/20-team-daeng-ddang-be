package com.daengddang.daengdong_map.controller.api;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.api.ErrorCodes;
import com.daengddang.daengdong_map.dto.request.s3.PresignedUrlRequest;
import com.daengddang.daengdong_map.dto.response.s3.PresignedUrlResponse;
import com.daengddang.daengdong_map.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "S3", description = "S3 presigned URL endpoints")
public interface S3Api {

    @Operation(summary = "Issue presigned URL")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INVALID_FORMAT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "FORBIDDEN"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "415", description = "FILE_TYPE_UNSUPPORTED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_FORMAT,
            com.daengddang.daengdong_map.common.ErrorCode.FORBIDDEN,
            com.daengddang.daengdong_map.common.ErrorCode.FILE_TYPE_UNSUPPORTED
    })
    ApiResponse<PresignedUrlResponse> issuePresignedUrl(
            @Parameter(hidden = true) AuthUser authUser,
            @RequestBody PresignedUrlRequest request
    );
}
