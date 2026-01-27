package com.daengddang.daengdong_map.dto.response.mission;

import com.daengddang.daengdong_map.domain.mission.Mission;
import java.util.List;
import lombok.Getter;

@Getter
public class MissionListResponse {

    private final List<MissionResponse> missions;

    private MissionListResponse(List<MissionResponse> missions) {
        this.missions = missions;
    }

    public static MissionListResponse from(List<Mission> missions) {
        List<MissionResponse> responses = missions.stream()
                .map(MissionResponse::from)
                .toList();
        return new MissionListResponse(responses);
    }
}
