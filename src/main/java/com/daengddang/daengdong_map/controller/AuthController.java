package com.daengddang.daengdong_map.controller;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.SuccessCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.controller.api.AuthApi;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.dto.request.auth.KakaoLoginRequest;
import com.daengddang.daengdong_map.dto.response.auth.AuthTokenResponse;
import com.daengddang.daengdong_map.dto.response.auth.TokenPair;
import com.daengddang.daengdong_map.security.oauth.kakao.model.KakaoOAuthUser;
import com.daengddang.daengdong_map.security.jwt.JwtTokenProvider;
import com.daengddang.daengdong_map.security.oauth.kakao.KakaoOAuthProperties;
import com.daengddang.daengdong_map.service.AuthService;
import com.daengddang.daengdong_map.service.AuthTokenService;
import com.daengddang.daengdong_map.service.KakaoOAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v3/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final AuthTokenService authTokenService;
    private final KakaoOAuthService kakaoOAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoOAuthProperties kakaoOAuthProperties;

    /**
     * 카카오 OAuth 로그인
     */
    @PostMapping("/login")
    @Override
    public ApiResponse<AuthTokenResponse> kakaoLogin(
            @RequestBody KakaoLoginRequest dto,
            HttpServletResponse response
    ) {
        KakaoOAuthUser oauthUser =
                kakaoOAuthService.authenticate(dto.getCode());

        AuthService.LoginResult loginResult = authService.loginOrRegister(oauthUser);
        User user = loginResult.user();
        TokenPair tokenPair = authTokenService.issueTokens(user);

        setRefreshTokenCookie(response, tokenPair.getRefreshToken());

        return ApiResponse.success(
                SuccessCode.LOGIN_SUCCESS,
                AuthTokenResponse.from(
                        tokenPair.getAccessToken(),
                        loginResult.isNewUser(),
                        user.getId()
                )
        );
    }

    /**
     * 카카오 로그인 페이지로 리다이렉트
     */
    @GetMapping
    @Override
    public void redirectToKakao(HttpServletResponse response) throws IOException {
        response.sendRedirect(buildAuthorizeUrl());
    }

    /**
     * 카카오 로그인 URL 반환 (프론트에서 사용)
     */
    @GetMapping("/authorize-url")
    @Override
    public ApiResponse<String> getAuthorizeUrl() {
        return ApiResponse.success(
                SuccessCode.KAKAO_LOGIN_URL_CREATED,
                buildAuthorizeUrl()
        );
    }

    /**
     * Access Token 재발급
     */
    @PostMapping("/token")
    @Override
    public ApiResponse<AuthTokenResponse> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        String newAccessToken =
                authTokenService.reissueAccessToken(refreshToken);

        return ApiResponse.success(
                SuccessCode.TOKEN_REFRESHED,
                AuthTokenResponse.from(
                        newAccessToken,
                        false,
                        null   // userId는 프론트에서 굳이 필요 없으면 null
                )
        );
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    @Override
    public ApiResponse<Void> logout(
            @RequestHeader("Authorization") String authorizationHeader,
            HttpServletResponse response
    ) {
        Long userId = extractUserId(authorizationHeader);

        authTokenService.logout(userId);
        clearRefreshTokenCookie(response);

        return ApiResponse.success(SuccessCode.LOGOUT_SUCCESS);
    }

    @GetMapping("/callback")
    @Override
    public ApiResponse<Map<String, String>> kakaoCallback(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "state", required = false) String state
    ) {
        if (code == null || code.isBlank()) {
            throw new BaseException(ErrorCode.AUTHORIZATION_CODE_INVALID_FORMAT);
        }

        Map<String, String> data = new LinkedHashMap<>();
        data.put("code", code);
        if (state != null && !state.isBlank()) {
            data.put("state", state);
        }

        return ApiResponse.success(SuccessCode.AUTHORIZATION_CODE_DELIVERED, data);
    }

    /* ================= Cookie ================= */

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 14);
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private Long extractUserId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        String token = authorizationHeader.substring(7);
        try {
            return Long.valueOf(jwtTokenProvider.parseClaims(token).getSubject());
        } catch (Exception e) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }
    }

    private String buildAuthorizeUrl() {
        if (kakaoOAuthProperties.getAuthorizeUri() == null
                || kakaoOAuthProperties.getClientId() == null
                || kakaoOAuthProperties.getRedirectUri() == null) {
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(kakaoOAuthProperties.getAuthorizeUri())
                .queryParam("client_id", kakaoOAuthProperties.getClientId())
                .queryParam("redirect_uri", kakaoOAuthProperties.getRedirectUri())
                .queryParam("response_type", "code");

        if (kakaoOAuthProperties.getScope() != null && !kakaoOAuthProperties.getScope().isBlank()) {
            builder.queryParam("scope", kakaoOAuthProperties.getScope());
        }

        return builder.build().toUriString();
    }
}
