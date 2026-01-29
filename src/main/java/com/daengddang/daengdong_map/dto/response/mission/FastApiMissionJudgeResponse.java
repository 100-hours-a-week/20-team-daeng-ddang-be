package com.daengddang.daengdong_map.dto.response.mission;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class FastApiMissionJudgeResponse {

    @JsonProperty("analysis_id")
    private String analysisId;

    @JsonProperty("walk_id")
    private Long walkId;

    @JsonProperty("analyzed_at")
    private OffsetDateTime analyzedAt;

    private List<MissionResult> missions;

    @Getter
    public static class MissionResult {

        @JsonProperty("mission_id")
        private Long missionId;

        @JsonProperty("mission_type")
        private String missionType;

        private Boolean success;

        private Float confidence;
    }
}
