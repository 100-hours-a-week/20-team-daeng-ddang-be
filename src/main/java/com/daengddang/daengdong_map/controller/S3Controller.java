package com.daengddang.daengdong_map.controller;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.SuccessCode;
import com.daengddang.daengdong_map.dto.request.s3.PresignedUrlRequest;
import com.daengddang.daengdong_map.dto.response.s3.PresignedUrlResponse;
import com.daengddang.daengdong_map.security.AuthUser;
import com.daengddang.daengdong_map.service.S3PresignedUrlService;
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
@RequestMapping("/api/v3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3PresignedUrlService s3PresignedUrlService;

    @PostMapping("/presigned-url")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PresignedUrlResponse> issuePresignedUrl(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody PresignedUrlRequest request
    ) {
        PresignedUrlResponse response =
                s3PresignedUrlService.issuePresignedUrl(authUser.getUserId(), request);
        return ApiResponse.success(SuccessCode.PRESIGNED_URL_ISSUED, response);
    }
}
