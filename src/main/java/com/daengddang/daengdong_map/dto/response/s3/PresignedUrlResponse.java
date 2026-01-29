package com.daengddang.daengdong_map.dto.response.s3;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PresignedUrlResponse {

    private final String presignedUrl;
    private final String objectKey;
    private final long expiresIn;

    @Builder
    private PresignedUrlResponse(String presignedUrl, String objectKey, long expiresIn) {
        this.presignedUrl = presignedUrl;
        this.objectKey = objectKey;
        this.expiresIn = expiresIn;
    }

    public static PresignedUrlResponse from(String presignedUrl, String objectKey, long expiresIn) {
        return PresignedUrlResponse.builder()
                .presignedUrl(presignedUrl)
                .objectKey(objectKey)
                .expiresIn(expiresIn)
                .build();
    }
}
