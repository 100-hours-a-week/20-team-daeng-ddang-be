package com.daengddang.daengdong_map.service.ranking.zset;

import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import com.daengddang.daengdong_map.domain.region.Region;
import com.daengddang.daengdong_map.service.ranking.batch.PeriodRange;
import com.daengddang.daengdong_map.service.ranking.batch.RankingPeriodResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingZsetRealtimeUpdater {

    private final RankingZsetProperties properties;
    private final RankingZsetKeyFactory keyFactory;
    private final RankingPeriodResolver periodResolver;
    private final RankingZsetMetrics metrics;
    private final RedissonClient redissonClient;

    public void addDistanceForFinishedWalk(Dog dog, double distanceMeters) {
        if (!properties.isEnabled()) {
            return;
        }
        if (dog == null || dog.getId() == null || distanceMeters <= 0.0d) {
            return;
        }

        try {
            PeriodRange weekRange = periodResolver.resolveCurrentPeriodRange(RankingPeriodType.WEEK);
            String periodValue = periodResolver.resolvePeriodValue(RankingPeriodType.WEEK, weekRange);
            String dogId = String.valueOf(dog.getId());

            addScore(keyFactory.dogGlobalKey(RankingPeriodType.WEEK, periodValue), dogId, distanceMeters);

            Long regionId = resolveRegionId(dog);
            if (regionId != null) {
                addScore(keyFactory.dogRegionKey(regionId, RankingPeriodType.WEEK, periodValue), dogId, distanceMeters);
            }

            metrics.recordUpdateSuccess();
        } catch (Exception e) {
            metrics.recordUpdateError();
            log.warn(
                    "실시간 랭킹 ZSET 업데이트 실패 (Realtime ranking zset update failed): dogId={}, distanceMeters={}",
                    dog.getId(),
                    distanceMeters,
                    e
            );
        }
    }

    private void addScore(String key, String dogId, double distanceMeters) {
        RScoredSortedSet<String> zset = redissonClient.getScoredSortedSet(key, StringCodec.INSTANCE);
        zset.addScore(dogId, distanceMeters);
        zset.expire(properties.getWeekTtlSeconds(), java.util.concurrent.TimeUnit.SECONDS);
    }

    private Long resolveRegionId(Dog dog) {
        if (dog.getUser() == null) {
            return null;
        }
        Region region = dog.getUser().getRegion();
        if (region == null) {
            return null;
        }
        return region.getId();
    }
}

