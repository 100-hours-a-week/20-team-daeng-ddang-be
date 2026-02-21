package com.daengddang.daengdong_map.dto.request.expression;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpressionAnalyzeRequest {

    @NotBlank
    private String videoUrl;

    private ExpressionAnalyzeRequest(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public static ExpressionAnalyzeRequest of(String videoUrl) {
        return new ExpressionAnalyzeRequest(videoUrl);
    }
}
