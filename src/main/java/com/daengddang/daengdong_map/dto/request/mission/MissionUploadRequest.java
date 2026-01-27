package com.daengddang.daengdong_map.dto.request.mission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class MissionUploadRequest {

    @NotNull
    private Long walkId;

    @NotNull
    private Long missionId;

    @NotBlank
    private String videoUrl;
}
