package com.daengddang.daengdong_map.service.ranking;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import com.daengddang.daengdong_map.domain.region.RegionStatus;
import com.daengddang.daengdong_map.dto.request.ranking.RankingCursorRequest;
import com.daengddang.daengdong_map.dto.request.ranking.RankingPeriodRegionRequest;
import com.daengddang.daengdong_map.dto.response.ranking.contribution.RegionContributionRankItemResponse;
import com.daengddang.daengdong_map.dto.response.ranking.contribution.RegionContributionRankingListResponse;
import com.daengddang.daengdong_map.dto.response.ranking.contribution.RegionContributionRankingSummaryResponse;
import com.daengddang.daengdong_map.repository.RegionDogRankRepository;
import com.daengddang.daengdong_map.repository.RegionRepository;
import com.daengddang.daengdong_map.repository.projection.RegionContributionRankView;
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
public class RegionContributionRankingService {

    private static final int SUMMARY_TOP_LIMIT = 3;

    private final RegionDogRankRepository regionDogRankRepository;
    private final RegionRepository regionRepository;
    private final AccessValidator accessValidator;

    public RegionContributionRankingSummaryResponse getRegionContributionRankingSummary(Long userId,
                                                                                        RankingPeriodRegionRequest dto) {
        validateRequest(dto);
        RankingPeriodType periodType = RankingValidator.parsePeriodType(dto.getPeriodType());
        RankingValidator.validatePeriodValue(periodType, dto.getPeriodValue());
        validateActiveRegion(dto.getRegionId());
        Dog dog = accessValidator.getDogOrThrow(userId);

        List<RegionContributionRankItemResponse> topRanks = regionDogRankRepository
                .findRanks(periodType, dto.getPeriodValue(), dto.getRegionId(), PageRequest.of(0, SUMMARY_TOP_LIMIT))
                .stream()
                .map(this::toContributionItem)
                .toList();

        RegionContributionRankItemResponse myRank = regionDogRankRepository
                .findMyRank(periodType, dto.getPeriodValue(), dto.getRegionId(), dog.getId())
                .map(this::toContributionItem)
                .orElse(null);

        return RegionContributionRankingSummaryResponse.of(topRanks, myRank);
    }

    public RegionContributionRankingListResponse getRegionContributionRankingList(Long userId,
                                                                                  RankingPeriodRegionRequest dto,
                                                                                  RankingCursorRequest cursorDto) {
        validateRequest(dto);
        RankingPeriodType periodType = RankingValidator.parsePeriodType(dto.getPeriodType());
        RankingValidator.validatePeriodValue(periodType, dto.getPeriodValue());
        validateActiveRegion(dto.getRegionId());
        accessValidator.getDogOrThrow(userId);

        String cursor = cursorDto == null ? null : cursorDto.getCursor();
        int limit = RankingValidator.resolveLimit(cursorDto == null ? null : cursorDto.getLimit());

        List<RegionContributionRankItemResponse> ranks;
        boolean hasNext;

        if (isBlank(cursor)) {
            int fetchSize = limit + 1;
            List<RegionContributionRankView> fetched = regionDogRankRepository
                    .findRanks(periodType, dto.getPeriodValue(), dto.getRegionId(), PageRequest.of(0, fetchSize));
            hasNext = fetched.size() > limit;
            ranks = fetched.stream()
                    .limit(limit)
                    .map(this::toContributionItem)
                    .toList();
        } else {
            RankingValidator.RateDogCursor parsedCursor = RankingValidator.parseRateDogCursor(cursor);
            Slice<RegionContributionRankView> slice = regionDogRankRepository.findRanksByCursor(
                    periodType,
                    dto.getPeriodValue(),
                    dto.getRegionId(),
                    parsedCursor.rate(),
                    parsedCursor.dogId(),
                    PageRequest.of(0, limit)
            );
            hasNext = slice.hasNext();
            ranks = slice.getContent()
                    .stream()
                    .map(this::toContributionItem)
                    .toList();
        }

        String nextCursor = hasNext && !ranks.isEmpty()
                ? toRateDogCursor(ranks.get(ranks.size() - 1).getContributionRate(), ranks.get(ranks.size() - 1).getDogId())
                : null;

        return RegionContributionRankingListResponse.of(ranks, nextCursor, hasNext);
    }

    private void validateRequest(RankingPeriodRegionRequest dto) {
        if (dto == null || dto.getRegionId() == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }
    }

    private void validateActiveRegion(Long regionId) {
        regionRepository.findByIdAndStatus(regionId, RegionStatus.ACTIVE)
                .orElseThrow(() -> new BaseException(ErrorCode.REGION_NOT_FOUND));
    }

    private RegionContributionRankItemResponse toContributionItem(RegionContributionRankView view) {
        return RegionContributionRankItemResponse.of(
                view.getRank(),
                view.getDogId(),
                view.getDogName(),
                view.getProfileImageUrl(),
                view.getDogDistance(),
                view.getContributionRate()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String toRateDogCursor(Double rate, Long dogId) {
        return "rate:" + toPlainNumber(rate) + ",dogId:" + dogId;
    }

    private String toPlainNumber(Double value) {
        if (value == null) {
            return "0";
        }
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
