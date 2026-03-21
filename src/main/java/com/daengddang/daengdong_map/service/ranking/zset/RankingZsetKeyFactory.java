package com.daengddang.daengdong_map.service.ranking.zset;

import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RankingZsetKeyFactory {

    private static final String SEP = ":";

    private final RankingZsetProperties properties;

    public String dogGlobalKey(RankingPeriodType periodType, String periodValue) {
        return prefix() + SEP + "dog" + SEP + "global" + SEP + periodType.name() + SEP + periodValue;
    }

    public String dogRegionKey(Long regionId, RankingPeriodType periodType, String periodValue) {
        return prefix() + SEP + "dog" + SEP + "region" + SEP + regionId + SEP + periodType.name() + SEP + periodValue;
    }

    public String dogMetaNameMapKey() {
        return prefix() + SEP + "meta" + SEP + "dog" + SEP + "name";
    }

    public String dogMetaPackedMapKey() {
        return prefix() + SEP + "meta" + SEP + "dog" + SEP + "packed";
    }

    public String dogMetaBirthDateMapKey() {
        return prefix() + SEP + "meta" + SEP + "dog" + SEP + "birthDate";
    }

    public String dogMetaProfileImageUrlMapKey() {
        return prefix() + SEP + "meta" + SEP + "dog" + SEP + "profileImageUrl";
    }

    public String dogMetaBreedMapKey() {
        return prefix() + SEP + "meta" + SEP + "dog" + SEP + "breed";
    }

    public String tempKey(String targetKey, String buildId) {
        return targetKey + SEP + "tmp" + SEP + buildId;
    }

    private String prefix() {
        return properties.getKeyVersion() + SEP + properties.getKeyPrefix();
    }
}
