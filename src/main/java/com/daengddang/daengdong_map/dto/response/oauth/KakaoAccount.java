package com.daengddang.daengdong_map.dto.response.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class KakaoAccount {

    @JsonProperty("has_email")
    private Boolean hasEmail;

    private String email;

    @JsonProperty("is_email_verified")
    private Boolean emailVerified;
}
