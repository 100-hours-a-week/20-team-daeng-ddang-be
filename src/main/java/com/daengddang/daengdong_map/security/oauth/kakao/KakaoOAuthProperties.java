package com.daengddang.daengdong_map.security.oauth.kakao;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "oauth.kakao")
public class KakaoOAuthProperties {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String scope;

    private String authorizeUri;
    private String tokenUri;
    private String userInfoUri;
}
