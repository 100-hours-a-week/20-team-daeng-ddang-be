package com.daengddang.daengdong_map.dto.response.mission;

import com.daengddang.daengdong_map.domain.mission.Mission;
import com.daengddang.daengdong_map.domain.mission.MissionDifficulty;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class MissionResponse {

    private final Long missionId;
    private final String title;
    private final String description;
    private final MissionDifficulty difficulty;
    private final String missionType;
    private final LocalDateTime createdAt;

    private MissionResponse(Long missionId,
                            String title,
                            String description,
                            MissionDifficulty difficulty,
                            String missionType,
                            LocalDateTime createdAt) {
        this.missionId = missionId;
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.missionType = missionType;
        this.createdAt = createdAt;
    }

    public static MissionResponse from(Mission mission) {
        return new MissionResponse(
                mission.getId(),
                mission.getTitle(),
                mission.getDescription(),
                mission.getDifficulty(),
                mission.getMissionType(),
                mission.getCreatedAt()
        );
    }
}
