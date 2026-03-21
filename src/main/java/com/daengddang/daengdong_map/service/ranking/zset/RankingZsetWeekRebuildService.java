package com.daengddang.daengdong_map.service.ranking.zset;

import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import com.daengddang.daengdong_map.repository.DogGlobalRankRepository;
import com.daengddang.daengdong_map.repository.DogRankRepository;
import com.daengddang.daengdong_map.repository.projection.DogDistanceScoreView;
import com.daengddang.daengdong_map.repository.projection.RegionDogDistanceScoreView;
import com.daengddang.daengdong_map.service.ranking.batch.PeriodRange;
import com.daengddang.daengdong_map.service.ranking.batch.RankingPeriodResolver;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingZsetWeekRebuildService {

    private final RankingZsetProperties properties;
    private final RankingZsetKeyFactory keyFactory;
    private final RankingPeriodResolver periodResolver;
    private final RankingZsetMetrics metrics;
    private final RedissonClient redissonClient;
    private final DogGlobalRankRepository dogGlobalRankRepository;
    private final DogRankRepository dogRankRepository;

    public void rebuildCurrentWeek() {
        if (!properties.isEnabled()) {
            return;
        }

        Instant startedAt = Instant.now();
        try {
            PeriodRange weekRange = periodResolver.resolveCurrentPeriodRange(RankingPeriodType.WEEK);
            String periodValue = periodResolver.resolvePeriodValue(RankingPeriodType.WEEK, weekRange);

            List<DogDistanceScoreView> globalScores = dogGlobalRankRepository.findScoresByPeriod(RankingPeriodType.WEEK, periodValue);
            long globalCount = replaceScores(
                    keyFactory.dogGlobalKey(RankingPeriodType.WEEK, periodValue),
                    globalScores.stream()
                            .map(score -> new ScoreEntry(score.getDogId(), score.getTotalDistance()))
                            .toList()
            );

            List<RegionDogDistanceScoreView> regionScores = dogRankRepository.findScoresByPeriod(RankingPeriodType.WEEK, periodValue);
            Map<Long, List<ScoreEntry>> byRegion = new LinkedHashMap<>();
            for (RegionDogDistanceScoreView row : regionScores) {
                if (row.getRegionId() == null || row.getDogId() == null) {
                    continue;
                }
                byRegion.computeIfAbsent(row.getRegionId(), ignored -> new ArrayList<>())
                        .add(new ScoreEntry(row.getDogId(), row.getTotalDistance()));
            }

            long regionKeyCount = 0;
            long regionMemberCount = 0;
            for (Map.Entry<Long, List<ScoreEntry>> entry : byRegion.entrySet()) {
                String key = keyFactory.dogRegionKey(entry.getKey(), RankingPeriodType.WEEK, periodValue);
                long memberCount = replaceScores(key, entry.getValue());
                regionKeyCount++;
                regionMemberCount += memberCount;
            }

            metrics.recordBuildSuccess(Duration.between(startedAt, Instant.now()));
            log.info(
                    "WEEK 랭킹 ZSET 재빌드 완료 (WEEK ranking zset rebuild completed): periodValue={}, globalMembers={}, regionKeys={}, regionMembers={}",
                    periodValue,
                    globalCount,
                    regionKeyCount,
                    regionMemberCount
            );
        } catch (Exception e) {
            metrics.recordBuildError(Duration.between(startedAt, Instant.now()));
            log.warn("WEEK 랭킹 ZSET 재빌드 실패 (WEEK ranking zset rebuild failed)", e);
        }
    }

    private long replaceScores(String targetKey, List<ScoreEntry> scores) {
        if (scores.isEmpty()) {
            redissonClient.getKeys().delete(targetKey);
            return 0;
        }

        String tempKey = keyFactory.tempKey(targetKey, UUID.randomUUID().toString());
        RScoredSortedSet<String> tempZset = redissonClient.getScoredSortedSet(tempKey, StringCodec.INSTANCE);
        try {
            for (ScoreEntry score : scores) {
                tempZset.add(score.totalDistance(), String.valueOf(score.dogId()));
            }
            tempZset.expire(properties.getWeekTtlSeconds(), TimeUnit.SECONDS);
            redissonClient.getKeys().rename(tempKey, targetKey);

            RScoredSortedSet<String> targetZset = redissonClient.getScoredSortedSet(targetKey, StringCodec.INSTANCE);
            targetZset.expire(properties.getWeekTtlSeconds(), TimeUnit.SECONDS);
            return scores.size();
        } finally {
            redissonClient.getKeys().delete(tempKey);
        }
    }

    private record ScoreEntry(Long dogId, double totalDistance) {
        private ScoreEntry(Long dogId, Double totalDistance) {
            this(dogId, totalDistance == null ? 0.0d : totalDistance);
        }
    }
}

