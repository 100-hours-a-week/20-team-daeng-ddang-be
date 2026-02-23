package com.daengddang.daengdong_map.controller;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.SuccessCode;
import com.daengddang.daengdong_map.controller.api.HealthcareChatApi;
import com.daengddang.daengdong_map.dto.request.chat.HealthcareChatRequest;
import com.daengddang.daengdong_map.dto.response.chat.HealthcareChatResponse;
import com.daengddang.daengdong_map.dto.response.chat.HealthcareChatSessionResponse;
import com.daengddang.daengdong_map.security.AuthUser;
import com.daengddang.daengdong_map.service.HealthcareChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v3/healthcares/chat")
@RequiredArgsConstructor
public class HealthcareChatController implements HealthcareChatApi {

    private final HealthcareChatService healthcareChatService;

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public ApiResponse<HealthcareChatSessionResponse> createSession(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        HealthcareChatSessionResponse response = healthcareChatService.createSession(authUser.getUserId());
        return ApiResponse.success(SuccessCode.CONSULT_SESSION_CREATED, response);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public ApiResponse<HealthcareChatResponse> sendMessage(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody HealthcareChatRequest request
    ) {
        HealthcareChatResponse response = healthcareChatService.sendMessage(authUser.getUserId(), request);
        return ApiResponse.success(SuccessCode.CONSULT_RESPONSE_CREATED, response);
    }
}
