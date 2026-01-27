package com.daengddang.daengdong_map.dto.request.mission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;

@Getter
public class MissionJudgeRequest {

    @NotNull
    private Long walkId;

    @NotEmpty
    private List<MissionItem> missions;

    @Getter
    public static class MissionItem {

        @NotNull
        private Long missionId;

        @NotBlank
        private String videoUrl;
    }
}
