package com.daengddang.daengdong_map.dto.request.mission;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;

@Getter
public class FastApiMissionJudgeRequest {

    @JsonProperty("analysis_id")
    @NotBlank
    private String analysisId;

    @JsonProperty("walk_id")
    @NotNull
    private Long walkId;

    @NotEmpty
    private List<MissionItem> missions;

    private FastApiMissionJudgeRequest(String analysisId, Long walkId, List<MissionItem> missions) {
        this.analysisId = analysisId;
        this.walkId = walkId;
        this.missions = missions;
    }

    public static FastApiMissionJudgeRequest of(String analysisId, Long walkId, List<MissionItem> missions) {
        return new FastApiMissionJudgeRequest(analysisId, walkId, missions);
    }

    @Getter
    public static class MissionItem {

        @JsonProperty("mission_id")
        @NotNull
        private Long missionId;

        @JsonProperty("mission_type")
        @NotBlank
        private String missionType;

        @JsonProperty("video_url")
        @NotBlank
        private String videoUrl;

        private MissionItem(Long missionId, String missionType, String videoUrl) {
            this.missionId = missionId;
            this.missionType = missionType;
            this.videoUrl = videoUrl;
        }

        public static MissionItem of(Long missionId, String missionType, String videoUrl) {
            return new MissionItem(missionId, missionType, videoUrl);
        }
    }
}
