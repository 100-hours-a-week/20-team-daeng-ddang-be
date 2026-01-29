package com.daengddang.daengdong_map.dto.response.user;

import lombok.Getter;

@Getter
public class UserRegisterResponse {

    private final Long userId;
    private final Long regionId;

    private UserRegisterResponse(Long userId, Long regionId) {
        this.userId = userId;
        this.regionId = regionId;
    }

    public static UserRegisterResponse from(Long userId, Long regionId) {
        return new UserRegisterResponse(userId, regionId);
    }
}
