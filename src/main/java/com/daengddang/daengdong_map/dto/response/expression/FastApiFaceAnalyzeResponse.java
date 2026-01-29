package com.daengddang.daengdong_map.dto.response.expression;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class FastApiFaceAnalyzeResponse {

    @JsonProperty("analysis_id")
    private String analysisId;

    @JsonProperty("predicted_emotion")
    private String predictedEmotion;

    private String summary;

    @JsonProperty("emotion_probabilities")
    private EmotionProbabilities emotionProbabilities;

    @JsonProperty("video_url")
    private String videoUrl;

    @Getter
    public static class EmotionProbabilities {

        private Double angry;
        private Double happy;
        private Double sad;
        private Double relaxed;
    }
}
