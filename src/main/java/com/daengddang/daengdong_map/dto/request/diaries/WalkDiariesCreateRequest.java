package com.daengddang.daengdong_map.dto.request.diaries;

import com.daengddang.daengdong_map.domain.diary.WalkDiary;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.domain.walk.Walk;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WalkDiariesCreateRequest {

    private String memo;

    private String mapImageUrl;

    public WalkDiariesCreateRequest(String memo, String mapImageUrl) {
        this.memo = memo;
        this.mapImageUrl = mapImageUrl;
    }

    public static WalkDiary of(WalkDiariesCreateRequest dto, User user, Walk walk) {
        return WalkDiary.builder()
                .memo(dto.getMemo())
                .mapImageUrl(dto.getMapImageUrl())
                .user(user)
                .walk(walk)
                .build();
    }

}
