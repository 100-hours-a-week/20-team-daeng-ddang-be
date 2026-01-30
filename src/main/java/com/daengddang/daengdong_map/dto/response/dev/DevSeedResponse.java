package com.daengddang.daengdong_map.dto.response.dev;

import java.util.List;
import lombok.Getter;

@Getter
public class DevSeedResponse {

    private final int created;
    private final List<Long> userIds;

    private DevSeedResponse(int created, List<Long> userIds) {
        this.created = created;
        this.userIds = userIds;
    }

    public static DevSeedResponse from(int created, List<Long> userIds) {
        return new DevSeedResponse(created, userIds);
    }
}
