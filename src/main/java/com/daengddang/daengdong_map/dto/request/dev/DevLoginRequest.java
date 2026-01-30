package com.daengddang.daengdong_map.dto.request.dev;

import lombok.Getter;

@Getter
public class DevLoginRequest {

    private Long kakaoUserId;
    private String nickname;
    private String prefix;
}
