package com.daengddang.daengdong_map.dto.response.diaries;

import com.daengddang.daengdong_map.domain.diary.WalkDiary;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class WalkDiariesCreateResponse {

    private Long walkDiaryId;
    private Long walkId;
    private LocalDateTime createdAt;

    @Builder
    public WalkDiariesCreateResponse(Long walkDiaryId, Long walkId, LocalDateTime createdAt) {
        this.walkDiaryId = walkDiaryId;
        this.walkId = walkId;
        this.createdAt = createdAt;
    }

    public static WalkDiariesCreateResponse from(WalkDiary walkDiary) {
        return WalkDiariesCreateResponse.builder()
                .walkDiaryId(walkDiary.getId())
                .walkId(walkDiary.getWalk().getId())
                .createdAt(walkDiary.getCreatedAt())
                .build();
    }
}
