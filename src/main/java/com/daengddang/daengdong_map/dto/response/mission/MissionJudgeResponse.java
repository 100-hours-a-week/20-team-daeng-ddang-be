package com.daengddang.daengdong_map.dto.response.mission;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MissionJudgeResponse {

    private final String analysisId;
    private final Long walkId;
    private final LocalDateTime analyzedAt;
    private final List<MissionResult> missions;

    @Builder
    private MissionJudgeResponse(String analysisId,
                                 Long walkId,
                                 LocalDateTime analyzedAt,
                                 List<MissionResult> missions) {
        this.analysisId = analysisId;
        this.walkId = walkId;
        this.analyzedAt = analyzedAt;
        this.missions = missions;
    }

    public static MissionJudgeResponse from(String analysisId,
                                            Long walkId,
                                            LocalDateTime analyzedAt,
                                            List<MissionResult> missions) {
        return MissionJudgeResponse.builder()
                .analysisId(analysisId)
                .walkId(walkId)
                .analyzedAt(analyzedAt)
                .missions(missions)
                .build();
    }

    @Getter
    public static class MissionResult {

        private final Long missionId;
        private final Boolean success;

        @Builder
        private MissionResult(Long missionId, Boolean success) {
            this.missionId = missionId;
            this.success = success;
        }

        public static MissionResult from(Long missionId, Boolean success) {
            return MissionResult.builder()
                    .missionId(missionId)
                    .success(success)
                    .build();
        }
    }
}
