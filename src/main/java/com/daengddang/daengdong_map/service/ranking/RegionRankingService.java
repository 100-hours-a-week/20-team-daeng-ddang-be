package com.daengddang.daengdong_map.service.ranking;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import com.daengddang.daengdong_map.domain.region.Region;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.dto.request.ranking.RankingCursorRequest;
import com.daengddang.daengdong_map.dto.request.ranking.RankingPeriodRequest;
import com.daengddang.daengdong_map.dto.request.ranking.RankingPeriodRegionRequest;
import com.daengddang.daengdong_map.dto.response.ranking.region.RegionRankItemResponse;
import com.daengddang.daengdong_map.dto.response.ranking.region.RegionRankingListResponse;
import com.daengddang.daengdong_map.dto.response.ranking.region.RegionRankingSummaryResponse;
import com.daengddang.daengdong_map.repository.RegionRankRepository;
import com.daengddang.daengdong_map.repository.projection.RegionRankView;
import com.daengddang.daengdong_map.util.AccessValidator;
import com.daengddang.daengdong_map.util.RankingCursorCodec;
import com.daengddang.daengdong_map.util.RankingRequestValidator;
import com.daengddang.daengdong_map.util.RegionValidator;
import com.daengddang.daengdong_map.util.RankingValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionRankingService {

    private static final int SUMMARY_TOP_LIMIT = 3;

    private final RegionRankRepository regionRankRepository;
    private final CursorPagingSupport cursorPagingSupport;
    private final RankingRequestValidator rankingRequestValidator;
    private final RankingCursorCodec rankingCursorCodec;
    private final RegionValidator regionValidator;
    private final AccessValidator accessValidator;

    public RegionRankingSummaryResponse getRegionRankingSummary(Long userId, RankingPeriodRegionRequest dto) {
        rankingRequestValidator.validateRequestNotNull(dto);
        RankingPeriodType periodType = rankingRequestValidator.parseAndValidatePeriod(dto.getPeriodType(), dto.getPeriodValue());
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
        rankingRequestValidator.validateRequestNotNull(dto);
        RankingPeriodType periodType = rankingRequestValidator.parseAndValidatePeriod(dto.getPeriodType(), dto.getPeriodValue());
        accessValidator.getUserOrThrow(userId);

        String cursor = rankingRequestValidator.resolveCursor(cursorDto);
        int limit = rankingRequestValidator.resolveLimit(cursorDto);

        CursorPagingSupport.CursorPageResult<RegionRankItemResponse> page = cursorPagingSupport.paginate(
                cursor,
                limit,
                fetchSize -> regionRankRepository.findRanks(periodType, dto.getPeriodValue(), PageRequest.of(0, fetchSize)),
                RankingValidator::parseDistanceRegionCursor,
                (parsedCursor, pageLimit) -> regionRankRepository.findRanksByCursor(
                        periodType,
                        dto.getPeriodValue(),
                        parsedCursor.distance(),
                        parsedCursor.regionId(),
                        PageRequest.of(0, pageLimit)
                ),
                this::toRegionRankItem,
                item -> rankingCursorCodec.toDistanceRegionCursor(item.getTotalDistance(), item.getRegionId())
        );

        return RegionRankingListResponse.of(page.items(), page.nextCursor(), page.hasNext());
    }

    private Long resolveRegionId(User user, Long requestedRegionId) {
        if (requestedRegionId != null) {
            regionValidator.validateActiveRegion(requestedRegionId);
            return requestedRegionId;
        }

        Region userRegion = user.getRegion();
        if (userRegion == null) {
            throw new BaseException(ErrorCode.REGION_NOT_FOUND);
        }

        regionValidator.validateActiveRegion(userRegion.getId());
        return userRegion.getId();
    }

    private RegionRankItemResponse toRegionRankItem(RegionRankView view) {
        return RegionRankItemResponse.of(
                view.getRank(),
                view.getRegionId(),
                view.getRegionName(),
                view.getTotalDistance()
        );
    }
}
