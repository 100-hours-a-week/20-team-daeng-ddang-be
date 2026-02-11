package com.daengddang.daengdong_map.util;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class RankingCursorCodec {

    public String toDistanceDogCursor(Double distance, Long dogId) {
        return "distance:" + toPlainNumber(distance) + ",dogId:" + dogId;
    }

    public String toDistanceRegionCursor(Double distance, Long regionId) {
        return "distance:" + toPlainNumber(distance) + ",regionId:" + regionId;
    }

    public String toRateDogCursor(Double rate, Long dogId) {
        return "rate:" + toPlainNumber(rate) + ",dogId:" + dogId;
    }

    private String toPlainNumber(Double value) {
        if (value == null) {
            return "0";
        }
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
