package com.daengddang.daengdong_map.dto.response.mission;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MissionUploadListResponse {

    private final List<MissionUploadResponse> uploads;

    @Builder
    private MissionUploadListResponse(List<MissionUploadResponse> uploads) {
        this.uploads = uploads;
    }

    public static MissionUploadListResponse from(List<MissionUploadResponse> uploads) {
        return MissionUploadListResponse.builder()
                .uploads(uploads)
                .build();
    }
}
