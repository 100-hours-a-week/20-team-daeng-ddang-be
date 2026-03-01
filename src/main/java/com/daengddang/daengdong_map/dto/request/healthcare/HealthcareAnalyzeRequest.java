package com.daengddang.daengdong_map.dto.request.healthcare;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HealthcareAnalyzeRequest {

    @NotBlank
    private String videoUrl;

    private HealthcareAnalyzeRequest(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public static HealthcareAnalyzeRequest of(String videoUrl) {
        return new HealthcareAnalyzeRequest(videoUrl);
    }
}
