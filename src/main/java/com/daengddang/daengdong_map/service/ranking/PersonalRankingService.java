package com.daengddang.daengdong_map.service.ranking;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import com.daengddang.daengdong_map.domain.region.RegionStatus;
import com.daengddang.daengdong_map.dto.request.ranking.RankingCursorRequest;
import com.daengddang.daengdong_map.dto.request.ranking.RankingPeriodRegionRequest;
import com.daengddang.daengdong_map.dto.response.ranking.personal.PersonalRankingListResponse;
import com.daengddang.daengdong_map.dto.response.ranking.personal.PersonalRankItemResponse;
import com.daengddang.daengdong_map.dto.response.ranking.personal.PersonalRankingSummaryResponse;
import com.daengddang.daengdong_map.repository.DogRankRepository;
import com.daengddang.daengdong_map.repository.RegionRepository;
import com.daengddang.daengdong_map.repository.projection.DogRankView;
import com.daengddang.daengdong_map.util.AccessValidator;
import com.daengddang.daengdong_map.util.RankingValidator;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalRankingService {

    private static final int SUMMARY_TOP_LIMIT = 3;

    private final DogRankRepository dogRankRepository;
    private final RegionRepository regionRepository;
    private final AccessValidator accessValidator;

    public PersonalRankingSummaryResponse getPersonalRankingSummary(Long userId, RankingPeriodRegionRequest dto) {
        validateRequest(dto);
        RankingPeriodType periodType = RankingValidator.parsePeriodType(dto.getPeriodType());
        RankingValidator.validatePeriodValue(periodType, dto.getPeriodValue());
        Dog dog = accessValidator.getDogOrThrow(userId);
        Long regionId = dto.getRegionId();

        List<PersonalRankItemResponse> topRanks;
        PersonalRankItemResponse myRank;
        if (regionId == null) {
            topRanks = dogRankRepository
                    .findRanksAllRegions(periodType, dto.getPeriodValue(), PageRequest.of(0, SUMMARY_TOP_LIMIT))
                    .stream()
                    .map(this::toPersonalRankItem)
                    .toList();

            myRank = dogRankRepository
                    .findMyRankAllRegions(periodType, dto.getPeriodValue(), dog.getId())
                    .map(this::toPersonalRankItem)
                    .orElse(null);
        } else {
            validateActiveRegion(regionId);
            topRanks = dogRankRepository
                    .findRanks(periodType, dto.getPeriodValue(), regionId, PageRequest.of(0, SUMMARY_TOP_LIMIT))
                    .stream()
                    .map(this::toPersonalRankItem)
                    .toList();

            myRank = dogRankRepository
                    .findMyRank(periodType, dto.getPeriodValue(), regionId, dog.getId())
                    .map(this::toPersonalRankItem)
                    .orElse(null);
        }

        return PersonalRankingSummaryResponse.of(topRanks, myRank);
    }

    public PersonalRankingListResponse getPersonalRankingList(Long userId,
                                                              RankingPeriodRegionRequest dto,
                                                              RankingCursorRequest cursorRequest) {
        validateRequest(dto);
        RankingPeriodType periodType = RankingValidator.parsePeriodType(dto.getPeriodType());
        RankingValidator.validatePeriodValue(periodType, dto.getPeriodValue());
        accessValidator.getDogOrThrow(userId);

        Long regionId = dto.getRegionId();
        if (regionId != null) {
            validateActiveRegion(regionId);
        }
        String cursor = cursorRequest == null ? null : cursorRequest.getCursor();
        int limit = RankingValidator.resolveLimit(cursorRequest == null ? null : cursorRequest.getLimit());

        List<PersonalRankItemResponse> ranks;
        boolean hasNext;

        if (isBlank(cursor)) {
            int fetchSize = limit + 1;
            List<DogRankView> fetched = (regionId == null)
                    ? dogRankRepository.findRanksAllRegions(periodType, dto.getPeriodValue(), PageRequest.of(0, fetchSize))
                    : dogRankRepository.findRanks(periodType, dto.getPeriodValue(), regionId, PageRequest.of(0, fetchSize));

            hasNext = fetched.size() > limit;
            ranks = fetched.stream()
                    .limit(limit)
                    .map(this::toPersonalRankItem)
                    .toList();
        } else {
            RankingValidator.DistanceDogCursor parsedCursor = RankingValidator.parseDistanceDogCursor(cursor);
            Slice<DogRankView> slice = (regionId == null)
                    ? dogRankRepository.findRanksByCursorAllRegions(
                    periodType,
                    dto.getPeriodValue(),
                    parsedCursor.distance(),
                    parsedCursor.dogId(),
                    PageRequest.of(0, limit)
            )
                    : dogRankRepository.findRanksByCursor(
                    periodType,
                    dto.getPeriodValue(),
                    regionId,
                    parsedCursor.distance(),
                    parsedCursor.dogId(),
                    PageRequest.of(0, limit)
            );

            hasNext = slice.hasNext();
            ranks = slice.getContent()
                    .stream()
                    .map(this::toPersonalRankItem)
                    .toList();
        }

        String nextCursor = hasNext && !ranks.isEmpty()
                ? toDistanceDogCursor(ranks.get(ranks.size() - 1).getTotalDistance(), ranks.get(ranks.size() - 1).getDogId())
                : null;

        return PersonalRankingListResponse.of(ranks, nextCursor, hasNext);
    }

    private void validateRequest(RankingPeriodRegionRequest request) {
        if (request == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }
    }

    private void validateActiveRegion(Long regionId) {
        regionRepository.findByIdAndStatus(regionId, RegionStatus.ACTIVE)
                .orElseThrow(() -> new BaseException(ErrorCode.REGION_NOT_FOUND));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String toDistanceDogCursor(Double distance, Long dogId) {
        return "distance:" + toPlainNumber(distance) + ",dogId:" + dogId;
    }

    private String toPlainNumber(Double value) {
        if (value == null) {
            return "0";
        }
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private PersonalRankItemResponse toPersonalRankItem(DogRankView view) {
        return PersonalRankItemResponse.of(
                view.getRank(),
                view.getDogId(),
                view.getDogName(),
                view.getProfileImageUrl(),
                view.getTotalDistance()
        );
    }
}
