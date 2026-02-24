package com.daengddang.daengdong_map.dto.response.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class FastApiHealthcareChatResponse {

    @JsonProperty("dog_id")
    private Integer dogId;

    @JsonProperty("conversation_id")
    private String conversationId;

    @JsonProperty("answered_at")
    private OffsetDateTime answeredAt;

    private String answer;

    private List<Citation> citations;

    @Getter
    public static class Citation {
        @JsonProperty("doc_id")
        private String docId;

        private String title;
        private Float score;
        private String snippet;
    }
}
