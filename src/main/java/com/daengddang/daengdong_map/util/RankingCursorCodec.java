package com.daengddang.daengdong_map.util;

import org.springframework.stereotype.Component;

@Component
public class RankingCursorCodec {

    public String toRankDogCursor(Integer rank, Long dogId) {
        return "rank:" + toInteger(rank) + ",dogId:" + dogId;
    }

    public String toRankRegionCursor(Integer rank, Long regionId) {
        return "rank:" + toInteger(rank) + ",regionId:" + regionId;
    }

    private String toInteger(Integer value) {
        if (value == null) {
            return "0";
        }
        return Integer.toString(value);
    }
}
