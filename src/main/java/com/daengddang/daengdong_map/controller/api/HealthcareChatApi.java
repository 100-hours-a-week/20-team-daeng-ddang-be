package com.daengddang.daengdong_map.controller.api;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.api.ErrorCodes;
import com.daengddang.daengdong_map.dto.request.chat.HealthcareChatRequest;
import com.daengddang.daengdong_map.dto.response.chat.HealthcareChatResponse;
import com.daengddang.daengdong_map.dto.response.chat.HealthcareChatSessionResponse;
import com.daengddang.daengdong_map.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Healthcare Chat", description = "Veterinary chatbot endpoints")
public interface HealthcareChatApi {

    @Operation(summary = "Create healthcare chat session")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "FORBIDDEN"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "AI_SERVER_CONNECTION_FAILED")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.FORBIDDEN,
            com.daengddang.daengdong_map.common.ErrorCode.AI_SERVER_CONNECTION_FAILED
    })
    ApiResponse<HealthcareChatSessionResponse> createSession(
            @Parameter(hidden = true) AuthUser authUser
    );

    @Operation(summary = "Send healthcare chat message")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INVALID_FORMAT, INVALID_FILE_URL"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "FORBIDDEN"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RESOURCE_NOT_FOUND"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "SESSION_EXPIRED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "415", description = "FILE_TYPE_UNSUPPORTED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "CHAT_ANALYSIS_NOT_SUPPORTED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "AI_SERVER_CONNECTION_FAILED")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_FORMAT,
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_FILE_URL,
            com.daengddang.daengdong_map.common.ErrorCode.FILE_TYPE_UNSUPPORTED,
            com.daengddang.daengdong_map.common.ErrorCode.FORBIDDEN,
            com.daengddang.daengdong_map.common.ErrorCode.RESOURCE_NOT_FOUND,
            com.daengddang.daengdong_map.common.ErrorCode.SESSION_EXPIRED,
            com.daengddang.daengdong_map.common.ErrorCode.CHAT_ANALYSIS_NOT_SUPPORTED,
            com.daengddang.daengdong_map.common.ErrorCode.AI_SERVER_CONNECTION_FAILED
    })
    ApiResponse<HealthcareChatResponse> sendMessage(
            @Parameter(hidden = true) AuthUser authUser,
            @RequestBody HealthcareChatRequest request
    );
}
