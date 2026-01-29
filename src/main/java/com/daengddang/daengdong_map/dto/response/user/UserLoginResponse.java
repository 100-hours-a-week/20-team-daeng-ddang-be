package com.daengddang.daengdong_map.dto.response.user;

import lombok.Getter;

@Getter
public class UserLoginResponse {

    private final Long userId;

    private UserLoginResponse(Long userId) {
        this.userId = userId;
    }

    public static UserLoginResponse from(Long userId) {
        return new UserLoginResponse(userId);
    }
}