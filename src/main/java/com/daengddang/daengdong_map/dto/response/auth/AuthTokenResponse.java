package com.daengddang.daengdong_map.dto.response.auth;

import com.daengddang.daengdong_map.dto.response.user.UserLoginResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class AuthTokenResponse {

    private final String accessToken;
    @Getter(AccessLevel.NONE)
    private final boolean isNewUser;
    private final UserLoginResponse user;

    private AuthTokenResponse(
            String accessToken,
            boolean isNewUser,
            UserLoginResponse user
    ) {
        this.accessToken = accessToken;
        this.isNewUser = isNewUser;
        this.user = user;
    }

    public static AuthTokenResponse from(
            String accessToken,
            boolean isNewUser,
            Long userId
    ) {
        return new AuthTokenResponse(
                accessToken,
                isNewUser,
                UserLoginResponse.from(userId)
        );
    }

    @JsonProperty("isNewUser")
    public boolean isNewUser() {
        return isNewUser;
    }
}
