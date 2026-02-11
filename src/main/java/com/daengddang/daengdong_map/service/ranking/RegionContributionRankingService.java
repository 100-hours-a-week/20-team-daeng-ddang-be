package com.daengddang.daengdong_map.service.ranking;

import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import com.daengddang.daengdong_map.dto.request.ranking.RankingCursorRequest;
import com.daengddang.daengdong_map.dto.request.ranking.RankingPeriodRegionRequest;
import com.daengddang.daengdong_map.dto.response.ranking.contribution.RegionContributionRankItemResponse;
import com.daengddang.daengdong_map.dto.response.ranking.contribution.RegionContributionRankingListResponse;
import com.daengddang.daengdong_map.dto.response.ranking.contribution.RegionContributionRankingSummaryResponse;
import com.daengddang.daengdong_map.repository.RegionDogRankRepository;
import com.daengddang.daengdong_map.repository.projection.RegionContributionRankView;
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
public class RegionContributionRankingService {

    private static final int SUMMARY_TOP_LIMIT = 3;

    private final RegionDogRankRepository regionDogRankRepository;
    private final CursorPagingSupport cursorPagingSupport;
    private final RankingRequestValidator rankingRequestValidator;
    private final RankingCursorCodec rankingCursorCodec;
    private final RegionValidator regionValidator;
    private final AccessValidator accessValidator;

    public RegionContributionRankingSummaryResponse getRegionContributionRankingSummary(Long userId,
                                                                                        RankingPeriodRegionRequest dto) {
        rankingRequestValidator.validateRequestWithRegionId(dto);
        RankingPeriodType periodType = rankingRequestValidator.parseAndValidatePeriod(dto.getPeriodType(), dto.getPeriodValue());
        regionValidator.validateActiveRegion(dto.getRegionId());
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
        rankingRequestValidator.validateRequestWithRegionId(dto);
        RankingPeriodType periodType = rankingRequestValidator.parseAndValidatePeriod(dto.getPeriodType(), dto.getPeriodValue());
        regionValidator.validateActiveRegion(dto.getRegionId());
        accessValidator.getDogOrThrow(userId);

        String cursor = rankingRequestValidator.resolveCursor(cursorDto);
        int limit = rankingRequestValidator.resolveLimit(cursorDto);

        CursorPagingSupport.CursorPageResult<RegionContributionRankItemResponse> page = cursorPagingSupport.paginate(
                cursor,
                limit,
                fetchSize -> regionDogRankRepository.findRanks(
                        periodType,
                        dto.getPeriodValue(),
                        dto.getRegionId(),
                        PageRequest.of(0, fetchSize)
                ),
                RankingValidator::parseRateDogCursor,
                (parsedCursor, pageLimit) -> regionDogRankRepository.findRanksByCursor(
                        periodType,
                        dto.getPeriodValue(),
                        dto.getRegionId(),
                        parsedCursor.rate(),
                        parsedCursor.dogId(),
                        PageRequest.of(0, pageLimit)
                ),
                this::toContributionItem,
                item -> rankingCursorCodec.toRateDogCursor(item.getContributionRate(), item.getDogId())
        );

        return RegionContributionRankingListResponse.of(page.items(), page.nextCursor(), page.hasNext());
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
}
