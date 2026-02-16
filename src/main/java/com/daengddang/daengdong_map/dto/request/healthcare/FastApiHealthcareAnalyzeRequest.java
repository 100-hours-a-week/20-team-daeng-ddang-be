package com.daengddang.daengdong_map.dto.request.healthcare;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class FastApiHealthcareAnalyzeRequest {

    @JsonProperty("video_url")
    @NotBlank
    private String videoUrl;

    @JsonProperty("analysis_id")
    @NotBlank
    private String analysisId;

    @JsonProperty("dog_id")
    @NotNull
    private Integer dogId;

    private FastApiHealthcareAnalyzeRequest(String videoUrl, String analysisId, Integer dogId) {
        this.videoUrl = videoUrl;
        this.analysisId = analysisId;
        this.dogId = dogId;
    }

    public static FastApiHealthcareAnalyzeRequest of(String videoUrl, String analysisId, Integer dogId) {
        return new FastApiHealthcareAnalyzeRequest(videoUrl, analysisId, dogId);
    }
}
