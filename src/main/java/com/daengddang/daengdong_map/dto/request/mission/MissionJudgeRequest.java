package com.daengddang.daengdong_map.dto.request.mission;

import lombok.Getter;

@Getter
public class MissionJudgeRequest {

    public static MissionJudgeRequest empty() {
        return new MissionJudgeRequest();
    }
}
