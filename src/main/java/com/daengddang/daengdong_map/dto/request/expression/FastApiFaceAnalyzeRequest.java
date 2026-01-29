package com.daengddang.daengdong_map.dto.request.expression;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class FastApiFaceAnalyzeRequest {

    @JsonProperty("analysis_id")
    private String analysisId;

    @JsonProperty("video_url")
    private String videoUrl;

    private FastApiFaceAnalyzeRequest(String analysisId, String videoUrl) {
        this.analysisId = analysisId;
        this.videoUrl = videoUrl;
    }

    public static FastApiFaceAnalyzeRequest of(String analysisId, String videoUrl) {
        return new FastApiFaceAnalyzeRequest(analysisId, videoUrl);
    }
}
