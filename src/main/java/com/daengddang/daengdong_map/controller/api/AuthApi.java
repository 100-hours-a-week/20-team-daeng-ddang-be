package com.daengddang.daengdong_map.controller.api;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.api.ErrorCodes;
import com.daengddang.daengdong_map.dto.request.auth.KakaoLoginRequest;
import com.daengddang.daengdong_map.dto.response.auth.AuthTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Auth", description = "Authentication and token endpoints")
public interface AuthApi {

    @Operation(summary = "Kakao OAuth login")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "AUTHORIZATION_CODE_INVALID_FORMAT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTHORIZATION_CODE_EXPIRED, INVALID_AUTHORIZATION_CODE"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "LOGIN_SERVER_COMMUNICATION_FAILED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.AUTHORIZATION_CODE_INVALID_FORMAT,
            com.daengddang.daengdong_map.common.ErrorCode.AUTHORIZATION_CODE_EXPIRED,
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_AUTHORIZATION_CODE,
            com.daengddang.daengdong_map.common.ErrorCode.LOGIN_SERVER_COMMUNICATION_FAILED
    })
    ApiResponse<AuthTokenResponse> kakaoLogin(
            KakaoLoginRequest dto,
            HttpServletResponse response
    );

    @Operation(summary = "Redirect to Kakao login page")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes
    void redirectToKakao(HttpServletResponse response) throws IOException;

    @Operation(summary = "Get Kakao authorize URL")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes
    ApiResponse<String> getAuthorizeUrl();

    @Operation(summary = "Refresh access token")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "INVALID_REFRESH_TOKEN, REFRESH_TOKEN_EXPIRED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_REFRESH_TOKEN,
            com.daengddang.daengdong_map.common.ErrorCode.REFRESH_TOKEN_EXPIRED
    })
    ApiResponse<AuthTokenResponse> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    );

    @Operation(summary = "Logout")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes
    ApiResponse<Void> logout(
            @RequestHeader("Authorization") String authorizationHeader,
            HttpServletResponse response
    );

    @Operation(summary = "Kakao OAuth callback")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "AUTHORIZATION_CODE_INVALID_FORMAT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.AUTHORIZATION_CODE_INVALID_FORMAT
    })
    ApiResponse<Map<String, String>> kakaoCallback(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "state", required = false) String state
    );
}
