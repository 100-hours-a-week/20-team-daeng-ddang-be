package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.security.oauth.kakao.KakaoClient;
import com.daengddang.daengdong_map.security.oauth.kakao.model.KakaoOAuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private final KakaoClient kakaoClient;

    public KakaoOAuthUser authenticate(String code) {
        return kakaoClient.authenticate(code);
    }
}
