package com.daengddang.daengdong_map.security.oauth.kakao.model;

import com.daengddang.daengdong_map.dto.response.oauth.KakaoUserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KakaoOAuthUser {

    private Long kakaoUserId;
    private String email;

    public KakaoOAuthUser(Long kakaoUserId) {
        this(kakaoUserId, null);
    }

    public static KakaoOAuthUser from(KakaoUserResponse response) {
        String email = null;
        if (response.getKakaoAccount() != null) {
            if (Boolean.TRUE.equals(response.getKakaoAccount().getHasEmail())) {
                email = response.getKakaoAccount().getEmail();
            }
        }
        return new KakaoOAuthUser(
                response.getId(),
                email
        );
    }
}
