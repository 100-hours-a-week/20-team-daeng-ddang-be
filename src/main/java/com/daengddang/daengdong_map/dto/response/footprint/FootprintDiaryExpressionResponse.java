package com.daengddang.daengdong_map.dto.response.footprint;

import com.daengddang.daengdong_map.domain.expression.Expression;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class FootprintDiaryExpressionResponse {

    @JsonProperty("expression_id")
    private final Long expressionId;

    @JsonProperty("predicted_emotion")
    private final String predictedEmotion;

    @JsonProperty("emotion_probabilities")
    private final EmotionProbabilities emotionProbabilities;

    @JsonProperty("summary")
    private final String summary;

    @JsonProperty("video_url")
    private final String videoUrl;

    @JsonProperty("created_at")
    private final LocalDateTime createdAt;

    @Builder
    private FootprintDiaryExpressionResponse(Long expressionId,
                                             String predictedEmotion,
                                             EmotionProbabilities emotionProbabilities,
                                             String summary,
                                             String videoUrl,
                                             LocalDateTime createdAt) {
        this.expressionId = expressionId;
        this.predictedEmotion = predictedEmotion;
        this.emotionProbabilities = emotionProbabilities;
        this.summary = summary;
        this.videoUrl = videoUrl;
        this.createdAt = createdAt;
    }

    public static FootprintDiaryExpressionResponse from(Expression expression) {
        return FootprintDiaryExpressionResponse.builder()
                .expressionId(expression.getId())
                .predictedEmotion(expression.getPredictedEmotion())
                .emotionProbabilities(EmotionProbabilities.from(
                        expression.getAngry(),
                        expression.getHappy(),
                        expression.getSad(),
                        expression.getRelaxed()
                ))
                .summary(expression.getSummary())
                .videoUrl(expression.getVideoUrl())
                .createdAt(expression.getCreatedAt())
                .build();
    }

    @Getter
    public static class EmotionProbabilities {

        private final Double angry;
        private final Double happy;
        private final Double sad;
        private final Double relaxed;

        @Builder
        private EmotionProbabilities(Double angry, Double happy, Double sad, Double relaxed) {
            this.angry = angry;
            this.happy = happy;
            this.sad = sad;
            this.relaxed = relaxed;
        }

        public static EmotionProbabilities from(Double angry, Double happy, Double sad, Double relaxed) {
            return EmotionProbabilities.builder()
                    .angry(angry)
                    .happy(happy)
                    .sad(sad)
                    .relaxed(relaxed)
                    .build();
        }
    }
}
