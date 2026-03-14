package com.daengddang.daengdong_map.service.ranking;

import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import com.daengddang.daengdong_map.dto.request.ranking.RankingCursorRequest;
import com.daengddang.daengdong_map.dto.request.ranking.RankingPeriodRegionRequest;
import com.daengddang.daengdong_map.dto.response.ranking.personal.PersonalRankingListResponse;
import com.daengddang.daengdong_map.dto.response.ranking.personal.PersonalRankItemResponse;
import com.daengddang.daengdong_map.dto.response.ranking.personal.PersonalRankingSummaryResponse;
import com.daengddang.daengdong_map.repository.DogGlobalRankRepository;
import com.daengddang.daengdong_map.repository.DogRankRepository;
import com.daengddang.daengdong_map.repository.projection.DogRankView;
import com.daengddang.daengdong_map.service.cache.RankingPersonalCacheMetrics;
import com.daengddang.daengdong_map.service.cache.RankingPersonalCacheStore;
import com.daengddang.daengdong_map.service.ranking.zset.RankingDogMetaCacheService;
import com.daengddang.daengdong_map.service.ranking.zset.RankingZsetKeyFactory;
import com.daengddang.daengdong_map.service.ranking.zset.RankingZsetProperties;
import com.daengddang.daengdong_map.util.AccessValidator;
import com.daengddang.daengdong_map.util.RankingCursorCodec;
import com.daengddang.daengdong_map.util.RankingRequestValidator;
import com.daengddang.daengdong_map.util.RegionValidator;
import com.daengddang.daengdong_map.util.RankingValidator;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.redisson.client.codec.StringCodec;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalRankingService {

    private static final int SUMMARY_TOP_LIMIT = 3;

    private final DogGlobalRankRepository dogGlobalRankRepository;
    private final DogRankRepository dogRankRepository;
    private final RankingDogMetaCacheService rankingDogMetaCacheService;
    private final CursorPagingSupport cursorPagingSupport;
    private final RankingRequestValidator rankingRequestValidator;
    private final RankingCursorCodec rankingCursorCodec;
    private final RegionValidator regionValidator;
    private final AccessValidator accessValidator;
    private final RankingPersonalCacheStore rankingPersonalCacheStore;
    private final RankingPersonalCacheMetrics rankingPersonalCacheMetrics;
    private final RankingZsetProperties rankingZsetProperties;
    private final RankingZsetKeyFactory rankingZsetKeyFactory;
    private final RedissonClient redissonClient;
    private final ConcurrentHashMap<String, CompletableFuture<PersonalRankingSummaryResponse>> summaryInFlight =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CompletableFuture<PersonalRankingListResponse>> listInFlight =
            new ConcurrentHashMap<>();

    public PersonalRankingSummaryResponse getPersonalRankingSummary(Long userId, RankingPeriodRegionRequest dto) {
        rankingRequestValidator.validateRequestNotNull(dto);
        RankingPeriodType periodType = rankingRequestValidator.parseAndValidatePeriod(dto.getPeriodType(), dto.getPeriodValue());
        Long regionId = dto.getRegionId();
        if (regionId != null) {
            regionValidator.validateActiveRegion(regionId);
        }

        if (shouldReadSummaryFromZset(periodType)) {
            return loadSummaryFromZset(userId, periodType, dto.getPeriodValue(), regionId);
        }

        Optional<PersonalRankingSummaryResponse> cached = rankingPersonalCacheStore.getSummary(
                periodType.name(),
                dto.getPeriodValue(),
                regionId,
                userId
        );
        if (cached.isPresent()) {
            return cached.get();
        }

        String inflightKey = rankingPersonalCacheStore.buildSummaryKey(
                periodType.name(),
                dto.getPeriodValue(),
                regionId,
                userId
        );
        return loadSummaryWithSingleFlight(inflightKey, userId, periodType, dto.getPeriodValue(), regionId);
    }

    private PersonalRankingSummaryResponse loadSummaryWithSingleFlight(String inflightKey,
                                                                       Long userId,
                                                                       RankingPeriodType periodType,
                                                                       String periodValue,
                                                                       Long regionId) {
        while (true) {
            CompletableFuture<PersonalRankingSummaryResponse> existing = summaryInFlight.get(inflightKey);
            if (existing != null) {
                return existing.join();
            }

            CompletableFuture<PersonalRankingSummaryResponse> created = new CompletableFuture<>();
            if (summaryInFlight.putIfAbsent(inflightKey, created) == null) {
                try {
                    PersonalRankingSummaryResponse response = loadSummaryFromDb(userId, periodType, periodValue, regionId);
                    rankingPersonalCacheStore.putSummary(periodType.name(), periodValue, regionId, userId, response);
                    created.complete(response);
                    return response;
                } catch (Exception e) {
                    created.completeExceptionally(e);
                    throw e;
                } finally {
                    summaryInFlight.remove(inflightKey, created);
                }
            }
        }
    }

    private PersonalRankingSummaryResponse loadSummaryFromDb(Long userId,
                                                             RankingPeriodType periodType,
                                                             String periodValue,
                                                             Long regionId) {
        rankingPersonalCacheMetrics.recordDbLoad();
        Dog dog = userId != null ? accessValidator.getDogOrThrow(userId) : null;

        List<PersonalRankItemResponse> topRanks;
        PersonalRankItemResponse myRank;
        if (regionId == null) {
            topRanks = dogGlobalRankRepository
                    .findRanks(periodType, periodValue, PageRequest.of(0, SUMMARY_TOP_LIMIT))
                    .stream()
                    .map(this::toPersonalRankItem)
                    .toList();

            myRank = dog == null
                    ? null
                    : dogGlobalRankRepository
                    .findMyRank(periodType, periodValue, dog.getId())
                    .map(this::toPersonalRankItem)
                    .orElse(null);
        } else {
            regionValidator.validateActiveRegion(regionId);
            topRanks = dogRankRepository
                    .findRanks(periodType, periodValue, regionId, PageRequest.of(0, SUMMARY_TOP_LIMIT))
                    .stream()
                    .map(this::toPersonalRankItem)
                    .toList();

            myRank = dog == null
                    ? null
                    : dogRankRepository
                    .findMyRank(periodType, periodValue, regionId, dog.getId())
                    .map(this::toPersonalRankItem)
                    .orElse(null);
        }

        return PersonalRankingSummaryResponse.of(topRanks, myRank);
    }

    private PersonalRankingSummaryResponse loadSummaryFromZset(Long userId,
                                                               RankingPeriodType periodType,
                                                               String periodValue,
                                                               Long regionId) {
        try {
            Dog myDog = userId != null ? accessValidator.getDogOrThrow(userId) : null;
            Long myDogId = myDog == null ? null : myDog.getId();

            String key = regionId == null
                    ? rankingZsetKeyFactory.dogGlobalKey(periodType, periodValue)
                    : rankingZsetKeyFactory.dogRegionKey(regionId, periodType, periodValue);
            RScoredSortedSet<String> zset = redissonClient.getScoredSortedSet(key, StringCodec.INSTANCE);

            List<ScoredEntry<String>> topEntries = new ArrayList<>(zset.entryRangeReversed(0, SUMMARY_TOP_LIMIT - 1));

            LinkedHashSet<Long> dogIds = new LinkedHashSet<>();
            for (ScoredEntry<String> entry : topEntries) {
                Long parsed = toLongOrNull(entry.getValue());
                if (parsed != null) {
                    dogIds.add(parsed);
                }
            }
            if (myDogId != null) {
                dogIds.add(myDogId);
            }

            Map<Long, RankingDogMetaCacheService.DogMeta> dogById = rankingDogMetaCacheService.getByDogIds(dogIds);

            List<PersonalRankItemResponse> topRanks = new ArrayList<>();
            for (int i = 0; i < topEntries.size(); i++) {
                ScoredEntry<String> entry = topEntries.get(i);
                Long dogId = toLongOrNull(entry.getValue());
                if (dogId == null) {
                    continue;
                }
                RankingDogMetaCacheService.DogMeta dog = dogById.get(dogId);
                if (dog == null) {
                    continue;
                }
                topRanks.add(PersonalRankItemResponse.of(
                        i + 1,
                        dogId,
                        dog.name(),
                        dog.birthDate(),
                        dog.profileImageUrl(),
                        dog.breed(),
                        entry.getScore() == null ? 0.0 : entry.getScore()
                ));
            }

            PersonalRankItemResponse myRank = null;
            if (myDogId != null) {
                Integer revRank = zset.revRank(String.valueOf(myDogId));
                if (revRank != null) {
                    RankingDogMetaCacheService.DogMeta dog = dogById.get(myDogId);
                    if (dog != null) {
                        Double score = null;
                        for (ScoredEntry<String> entry : topEntries) {
                            if (String.valueOf(myDogId).equals(entry.getValue())) {
                                score = entry.getScore();
                                break;
                            }
                        }
                        if (score == null) {
                            score = zset.getScore(String.valueOf(myDogId));
                        }
                        myRank = PersonalRankItemResponse.of(
                                revRank + 1,
                                myDogId,
                                dog.name(),
                                dog.birthDate(),
                                dog.profileImageUrl(),
                                dog.breed(),
                                score == null ? 0.0 : score
                        );
                    }
                }
            }

            return PersonalRankingSummaryResponse.of(topRanks, myRank);
        } catch (Exception e) {
            return loadSummaryFromDb(userId, periodType, periodValue, regionId);
        }
    }

    public PersonalRankingListResponse getPersonalRankingList(Long userId,
                                                              RankingPeriodRegionRequest dto,
                                                              RankingCursorRequest cursorRequest) {
        rankingRequestValidator.validateRequestNotNull(dto);
        RankingPeriodType periodType = rankingRequestValidator.parseAndValidatePeriod(dto.getPeriodType(), dto.getPeriodValue());

        Long regionId = dto.getRegionId();
        if (regionId != null) {
            regionValidator.validateActiveRegion(regionId);
        }
        String cursor = rankingRequestValidator.resolveCursor(cursorRequest);
        int limit = rankingRequestValidator.resolveLimit(cursorRequest);

        if (shouldReadSummaryFromZset(periodType)) {
            return loadListFromZset(periodType, dto.getPeriodValue(), regionId, cursor, limit);
        }

        Optional<PersonalRankingListResponse> cached = rankingPersonalCacheStore.getList(
                periodType.name(),
                dto.getPeriodValue(),
                regionId,
                cursor,
                limit
        );
        if (cached.isPresent()) {
            return cached.get();
        }

        String inflightKey = rankingPersonalCacheStore.buildListKey(
                periodType.name(),
                dto.getPeriodValue(),
                regionId,
                cursor,
                limit
        );
        return loadListWithSingleFlight(inflightKey, periodType, dto.getPeriodValue(), regionId, cursor, limit);
    }

    private PersonalRankingListResponse loadListWithSingleFlight(String inflightKey,
                                                                 RankingPeriodType periodType,
                                                                 String periodValue,
                                                                 Long regionId,
                                                                 String cursor,
                                                                 int limit) {
        while (true) {
            CompletableFuture<PersonalRankingListResponse> existing = listInFlight.get(inflightKey);
            if (existing != null) {
                return existing.join();
            }

            CompletableFuture<PersonalRankingListResponse> created = new CompletableFuture<>();
            if (listInFlight.putIfAbsent(inflightKey, created) == null) {
                try {
                    PersonalRankingListResponse response = loadListFromDb(periodType, periodValue, regionId, cursor, limit);
                    rankingPersonalCacheStore.putList(periodType.name(), periodValue, regionId, cursor, limit, response);
                    created.complete(response);
                    return response;
                } catch (Exception e) {
                    created.completeExceptionally(e);
                    throw e;
                } finally {
                    listInFlight.remove(inflightKey, created);
                }
            }
        }
    }

    private PersonalRankingListResponse loadListFromDb(RankingPeriodType periodType,
                                                       String periodValue,
                                                       Long regionId,
                                                       String cursor,
                                                       int limit) {
        rankingPersonalCacheMetrics.recordDbLoad();
        CursorPagingSupport.CursorPageResult<PersonalRankItemResponse> page = cursorPagingSupport.paginate(
                cursor,
                limit,
                fetchSize -> regionId == null
                        ? dogGlobalRankRepository.findRanks(periodType, periodValue, PageRequest.of(0, fetchSize))
                        : dogRankRepository.findRanks(periodType, periodValue, regionId, PageRequest.of(0, fetchSize)),
                RankingValidator::parseRankDogCursor,
                (parsedCursor, pageLimit) -> regionId == null
                        ? dogGlobalRankRepository.findRanksByCursor(
                        periodType,
                        periodValue,
                        parsedCursor.rank(),
                        parsedCursor.dogId(),
                        PageRequest.of(0, pageLimit)
                )
                        : dogRankRepository.findRanksByCursor(
                        periodType,
                        periodValue,
                        regionId,
                        parsedCursor.rank(),
                        parsedCursor.dogId(),
                        PageRequest.of(0, pageLimit)
                ),
                this::toPersonalRankItem,
                item -> rankingCursorCodec.toRankDogCursor(item.getRank(), item.getDogId())
        );

        return PersonalRankingListResponse.of(page.items(), page.nextCursor(), page.hasNext());
    }

    private PersonalRankingListResponse loadListFromZset(RankingPeriodType periodType,
                                                         String periodValue,
                                                         Long regionId,
                                                         String cursor,
                                                         int limit) {
        try {
            String key = regionId == null
                    ? rankingZsetKeyFactory.dogGlobalKey(periodType, periodValue)
                    : rankingZsetKeyFactory.dogRegionKey(regionId, periodType, periodValue);
            RScoredSortedSet<String> zset = redissonClient.getScoredSortedSet(key, StringCodec.INSTANCE);

            int startIndex = 0;
            if (cursor != null && !cursor.isBlank()) {
                RankingValidator.RankDogCursor parsed = RankingValidator.parseRankDogCursor(cursor);
                startIndex = Math.max(parsed.rank(), 0);
            }

            int endIndexWithExtra = Math.max(startIndex + limit, startIndex);
            List<ScoredEntry<String>> entries = new ArrayList<>(zset.entryRangeReversed(startIndex, endIndexWithExtra));
            boolean hasNext = entries.size() > limit;
            int realSize = Math.min(entries.size(), limit);

            LinkedHashSet<Long> dogIds = new LinkedHashSet<>();
            for (int i = 0; i < realSize; i++) {
                Long parsed = toLongOrNull(entries.get(i).getValue());
                if (parsed != null) {
                    dogIds.add(parsed);
                }
            }
            Map<Long, RankingDogMetaCacheService.DogMeta> dogById = rankingDogMetaCacheService.getByDogIds(dogIds);

            List<PersonalRankItemResponse> ranks = new ArrayList<>(realSize);
            for (int i = 0; i < realSize; i++) {
                ScoredEntry<String> entry = entries.get(i);
                Long dogId = toLongOrNull(entry.getValue());
                if (dogId == null) {
                    continue;
                }
                RankingDogMetaCacheService.DogMeta dog = dogById.get(dogId);
                if (dog == null) {
                    continue;
                }
                ranks.add(PersonalRankItemResponse.of(
                        startIndex + i + 1,
                        dogId,
                        dog.name(),
                        dog.birthDate(),
                        dog.profileImageUrl(),
                        dog.breed(),
                        entry.getScore() == null ? 0.0 : entry.getScore()
                ));
            }

            String nextCursor = null;
            if (hasNext && !ranks.isEmpty()) {
                PersonalRankItemResponse last = ranks.get(ranks.size() - 1);
                nextCursor = rankingCursorCodec.toRankDogCursor(last.getRank(), last.getDogId());
            }
            return PersonalRankingListResponse.of(ranks, nextCursor, hasNext);
        } catch (Exception e) {
            return loadListFromDb(periodType, periodValue, regionId, cursor, limit);
        }
    }

    private PersonalRankItemResponse toPersonalRankItem(DogRankView view) {
        return PersonalRankItemResponse.of(
                view.getRank(),
                view.getDogId(),
                view.getDogName(),
                view.getBirthDate(),
                view.getProfileImageUrl(),
                view.getDogBreed(),
                view.getTotalDistance()
        );
    }

    private boolean shouldReadSummaryFromZset(RankingPeriodType periodType) {
        return rankingZsetProperties.isEnabled()
                && periodType == RankingPeriodType.WEEK
                && "zset".equalsIgnoreCase(rankingZsetProperties.getReadSource());
    }

    private Long toLongOrNull(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }
}
