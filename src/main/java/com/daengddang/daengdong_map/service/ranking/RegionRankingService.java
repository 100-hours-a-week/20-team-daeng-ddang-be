package com.daengddang.daengdong_map.service.ranking;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import com.daengddang.daengdong_map.domain.region.Region;
import com.daengddang.daengdong_map.domain.region.RegionStatus;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.dto.request.ranking.RankingCursorRequest;
import com.daengddang.daengdong_map.dto.request.ranking.RankingPeriodRequest;
import com.daengddang.daengdong_map.dto.request.ranking.RankingPeriodRegionRequest;
import com.daengddang.daengdong_map.dto.response.ranking.region.RegionRankItemResponse;
import com.daengddang.daengdong_map.dto.response.ranking.region.RegionRankingListResponse;
import com.daengddang.daengdong_map.dto.response.ranking.region.RegionRankingSummaryResponse;
import com.daengddang.daengdong_map.repository.RegionRankRepository;
import com.daengddang.daengdong_map.repository.RegionRepository;
import com.daengddang.daengdong_map.repository.projection.RegionRankView;
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
public class RegionRankingService {

    private static final int SUMMARY_TOP_LIMIT = 3;

    private final RegionRankRepository regionRankRepository;
    private final RegionRepository regionRepository;
    private final AccessValidator accessValidator;

    public RegionRankingSummaryResponse getRegionRankingSummary(Long userId, RankingPeriodRegionRequest dto) {
        validateRequest(dto);
        RankingPeriodType periodType = RankingValidator.parsePeriodType(dto.getPeriodType());
        RankingValidator.validatePeriodValue(periodType, dto.getPeriodValue());
        User user = accessValidator.getUserOrThrow(userId);

        Long regionId = resolveRegionId(user, dto.getRegionId());

        List<RegionRankItemResponse> topRanks = regionRankRepository
                .findRanks(periodType, dto.getPeriodValue(), PageRequest.of(0, SUMMARY_TOP_LIMIT))
                .stream()
                .map(this::toRegionRankItem)
                .toList();

        RegionRankItemResponse myRank = regionRankRepository
                .findMyRegionRank(periodType, dto.getPeriodValue(), regionId)
                .map(this::toRegionRankItem)
                .orElse(null);

        return RegionRankingSummaryResponse.of(topRanks, myRank);
    }

    public RegionRankingListResponse getRegionRankingList(Long userId,
                                                          RankingPeriodRequest dto,
                                                          RankingCursorRequest cursorDto) {
        validatePeriodRequest(dto);
        RankingPeriodType periodType = RankingValidator.parsePeriodType(dto.getPeriodType());
        RankingValidator.validatePeriodValue(periodType, dto.getPeriodValue());
        accessValidator.getUserOrThrow(userId);

        String cursor = cursorDto == null ? null : cursorDto.getCursor();
        int limit = RankingValidator.resolveLimit(cursorDto == null ? null : cursorDto.getLimit());

        List<RegionRankItemResponse> ranks;
        boolean hasNext;

        if (isBlank(cursor)) {
            int fetchSize = limit + 1;
            List<RegionRankView> fetched = regionRankRepository
                    .findRanks(periodType, dto.getPeriodValue(), PageRequest.of(0, fetchSize));
            hasNext = fetched.size() > limit;
            ranks = fetched.stream()
                    .limit(limit)
                    .map(this::toRegionRankItem)
                    .toList();
        } else {
            RankingValidator.DistanceRegionCursor parsedCursor = RankingValidator.parseDistanceRegionCursor(cursor);
            Slice<RegionRankView> slice = regionRankRepository.findRanksByCursor(
                    periodType,
                    dto.getPeriodValue(),
                    parsedCursor.distance(),
                    parsedCursor.regionId(),
                    PageRequest.of(0, limit)
            );
            hasNext = slice.hasNext();
            ranks = slice.getContent()
                    .stream()
                    .map(this::toRegionRankItem)
                    .toList();
        }

        String nextCursor = hasNext && !ranks.isEmpty()
                ? toDistanceRegionCursor(ranks.get(ranks.size() - 1).getTotalDistance(), ranks.get(ranks.size() - 1).getRegionId())
                : null;

        return RegionRankingListResponse.of(ranks, nextCursor, hasNext);
    }

    private void validateRequest(RankingPeriodRegionRequest dto) {
        if (dto == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }
    }

    private void validatePeriodRequest(RankingPeriodRequest dto) {
        if (dto == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }
    }

    private Long resolveRegionId(User user, Long requestedRegionId) {
        if (requestedRegionId != null) {
            validateActiveRegion(requestedRegionId);
            return requestedRegionId;
        }

        Region userRegion = user.getRegion();
        if (userRegion == null) {
            throw new BaseException(ErrorCode.REGION_NOT_FOUND);
        }

        validateActiveRegion(userRegion.getId());
        return userRegion.getId();
    }

    private void validateActiveRegion(Long regionId) {
        regionRepository.findByIdAndStatus(regionId, RegionStatus.ACTIVE)
                .orElseThrow(() -> new BaseException(ErrorCode.REGION_NOT_FOUND));
    }

    private RegionRankItemResponse toRegionRankItem(RegionRankView view) {
        return RegionRankItemResponse.of(
                view.getRank(),
                view.getRegionId(),
                view.getRegionName(),
                view.getTotalDistance()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String toDistanceRegionCursor(Double distance, Long regionId) {
        return "distance:" + toPlainNumber(distance) + ",regionId:" + regionId;
    }

    private String toPlainNumber(Double value) {
        if (value == null) {
            return "0";
        }
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
