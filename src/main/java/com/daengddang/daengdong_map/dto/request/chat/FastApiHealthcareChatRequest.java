package com.daengddang.daengdong_map.dto.request.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;

@Getter
public class FastApiHealthcareChatRequest {

    @JsonProperty("dog_id")
    @NotNull
    private Integer dogId;

    @JsonProperty("conversation_id")
    @NotBlank
    private String conversationId;

    @NotBlank
    private String message;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("user_context")
    @NotNull
    private UserContext userContext;

    @NotNull
    private List<HistoryItem> history;

    private FastApiHealthcareChatRequest(Integer dogId,
                                         String conversationId,
                                         String message,
                                         String imageUrl,
                                         UserContext userContext,
                                         List<HistoryItem> history) {
        this.dogId = dogId;
        this.conversationId = conversationId;
        this.message = message;
        this.imageUrl = imageUrl;
        this.userContext = userContext;
        this.history = history;
    }

    public static FastApiHealthcareChatRequest of(Integer dogId,
                                                  String conversationId,
                                                  String message,
                                                  String imageUrl,
                                                  UserContext userContext,
                                                  List<HistoryItem> history) {
        return new FastApiHealthcareChatRequest(
                dogId,
                conversationId,
                message,
                imageUrl,
                userContext,
                history
        );
    }

    @Getter
    public static class UserContext {

        @JsonProperty("dog_age_years")
        @NotNull
        private Integer dogAgeYears;

        @JsonProperty("dog_weight_kg")
        @NotNull
        private Integer dogWeightKg;

        @NotBlank
        private String breed;

        private UserContext(Integer dogAgeYears, Integer dogWeightKg, String breed) {
            this.dogAgeYears = dogAgeYears;
            this.dogWeightKg = dogWeightKg;
            this.breed = breed;
        }

        public static UserContext of(Integer dogAgeYears, Integer dogWeightKg, String breed) {
            return new UserContext(dogAgeYears, dogWeightKg, breed);
        }
    }

    @Getter
    public static class HistoryItem {

        @NotBlank
        private String role;

        @NotBlank
        private String content;

        private HistoryItem(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public static HistoryItem of(String role, String content) {
            return new HistoryItem(role, content);
        }
    }
}
