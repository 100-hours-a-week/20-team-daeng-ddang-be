package com.daengddang.daengdong_map.dto.response.mission;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MissionUploadResponse {

    private final Long missionUploadId;
    private final Long walkId;
    private final Long missionId;
    private final String videoUrl;
    private final LocalDateTime uploadedAt;

    @Builder
    private MissionUploadResponse(Long missionUploadId,
                                  Long walkId,
                                  Long missionId,
                                  String videoUrl,
                                  LocalDateTime uploadedAt) {
        this.missionUploadId = missionUploadId;
        this.walkId = walkId;
        this.missionId = missionId;
        this.videoUrl = videoUrl;
        this.uploadedAt = uploadedAt;
    }

    public static MissionUploadResponse from(Long missionUploadId,
                                             Long walkId,
                                             Long missionId,
                                             String videoUrl,
                                             LocalDateTime uploadedAt) {
        return MissionUploadResponse.builder()
                .missionUploadId(missionUploadId)
                .walkId(walkId)
                .missionId(missionId)
                .videoUrl(videoUrl)
                .uploadedAt(uploadedAt)
                .build();
    }
}
