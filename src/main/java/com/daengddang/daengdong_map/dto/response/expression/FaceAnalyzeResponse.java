package com.daengddang.daengdong_map.dto.response.expression;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
public class FaceAnalyzeResponse {

    @JsonProperty("analysis_id")
    private final String analysisId;

    @JsonProperty("predicted_emotion")
    private final String predictedEmotion;

    @JsonProperty("summary")
    private final String summary;

    @JsonProperty("video_url")
    private final String videoUrl;

    @JsonProperty("emotion_probabilities")
    private final EmotionProbabilities emotionProbabilities;

    @Builder
    private FaceAnalyzeResponse(String analysisId,
                                String predictedEmotion,
                                String summary,
                                String videoUrl,
                                EmotionProbabilities emotionProbabilities) {
        this.analysisId = analysisId;
        this.predictedEmotion = predictedEmotion;
        this.videoUrl = videoUrl;
        this.summary = summary;
        this.emotionProbabilities = emotionProbabilities;
    }

    public static FaceAnalyzeResponse from(String analysisId,
                                           String predictedEmotion,
                                           String videoUrl,
                                           String summary,
                                           EmotionProbabilities emotionProbabilities) {
        return FaceAnalyzeResponse.builder()
                .analysisId(analysisId)
                .predictedEmotion(predictedEmotion)
                .videoUrl(videoUrl)
                .summary(summary)
                .emotionProbabilities(emotionProbabilities)
                .build();
    }

    @Getter
    public static class EmotionProbabilities {

        private final Double angry;
        private final Double happy;
        private final Double sad;
        private final Double relaxed;

        @Builder
        private EmotionProbabilities(Double angry,
                                     Double happy,
                                     Double sad,
                                     Double relaxed) {
            this.angry = angry;
            this.happy = happy;
            this.sad = sad;
            this.relaxed = relaxed;
        }

        public static EmotionProbabilities from(Double angry,
                                                 Double happy,
                                                 Double sad,
                                                 Double relaxed) {
            return EmotionProbabilities.builder()
                    .angry(angry)
                    .happy(happy)
                    .sad(sad)
                    .relaxed(relaxed)
                    .build();
        }
    }
}
