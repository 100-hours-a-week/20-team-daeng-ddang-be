package com.daengddang.daengdong_map.controller;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.SuccessCode;
import com.daengddang.daengdong_map.dto.request.dev.DevLoginRequest;
import com.daengddang.daengdong_map.dto.request.dev.DevSeedRequest;
import com.daengddang.daengdong_map.dto.response.auth.AuthTokenResponse;
import com.daengddang.daengdong_map.dto.response.auth.TokenPair;
import com.daengddang.daengdong_map.dto.response.dev.DevSeedResponse;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.service.AuthTokenService;
import com.daengddang.daengdong_map.service.DevAuthService;
import jakarta.validation.Valid;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v3/auth/dev")
@RequiredArgsConstructor
@Profile({"local", "dev"})
public class DevAuthController {

    private final DevAuthService devAuthService;
    private final AuthTokenService authTokenService;

    /** 개발용: 사용자 1명 생성(또는 existing) + 토큰 발급 */
    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> devLogin(
            @RequestBody(required = false) DevLoginRequest req,
            HttpServletResponse response
    ) {
        Long kakaoUserId = req == null ? null : req.getKakaoUserId();
        String nickname = req == null ? null : req.getNickname();
        String prefix = req == null ? null : req.getPrefix();

        User user = devAuthService.getOrCreate(kakaoUserId, nickname, prefix);

        TokenPair tokenPair = authTokenService.issueTokens(user);
        setRefreshTokenCookie(response, tokenPair.getRefreshToken());

        return ApiResponse.success(
                SuccessCode.LOGIN_SUCCESS,
                AuthTokenResponse.from(tokenPair.getAccessToken(), false, user.getId())
        );
    }

    /** 개발용: N명 생성 (현실성 있는 seed) */
    @PostMapping("/seed")
    public ApiResponse<DevSeedResponse> seed(
            @Valid @RequestBody DevSeedRequest req
    ) {
        List<Long> userIds = devAuthService.seedUsers(req.getCount(), req.getPrefix());

        return ApiResponse.success(
                SuccessCode.DEV_USERS_SEEDED,
                DevSeedResponse.from(userIds.size(), userIds)
        );
    }

    /** 🔥 부하테스트 전용: FAST seed */
    @PostMapping("/seed/fast")
    public ApiResponse<DevSeedResponse> seedFast(
            @Valid @RequestBody DevSeedRequest req
    ) {
        List<Long> userIds = devAuthService.seedUsersFast(req.getCount(), req.getPrefix());

        return ApiResponse.success(
                SuccessCode.DEV_USERS_SEEDED,
                DevSeedResponse.from(userIds.size(), userIds)
        );
    }

    /** 개발용: 특정 userId로 토큰 발급 */
    @PostMapping("/token/{userId}")
    public ApiResponse<AuthTokenResponse> issueToken(
            @PathVariable Long userId,
            HttpServletResponse response
    ) {
        User user = devAuthService.getUserById(userId);

        TokenPair tokenPair = authTokenService.issueTokens(user);
        setRefreshTokenCookie(response, tokenPair.getRefreshToken());

        return ApiResponse.success(
                SuccessCode.LOGIN_SUCCESS,
                AuthTokenResponse.from(tokenPair.getAccessToken(), false, user.getId())
        );
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // dev/local only
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 14);
        response.addCookie(cookie);
    }
}