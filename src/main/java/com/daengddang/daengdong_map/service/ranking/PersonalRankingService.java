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
public class PersonalRankingService {

    private static final int SUMMARY_TOP_LIMIT = 3;

    private final DogGlobalRankRepository dogGlobalRankRepository;
    private final DogRankRepository dogRankRepository;
    private final CursorPagingSupport cursorPagingSupport;
    private final RankingRequestValidator rankingRequestValidator;
    private final RankingCursorCodec rankingCursorCodec;
    private final RegionValidator regionValidator;
    private final AccessValidator accessValidator;

    public PersonalRankingSummaryResponse getPersonalRankingSummary(Long userId, RankingPeriodRegionRequest dto) {
        rankingRequestValidator.validateRequestNotNull(dto);
        RankingPeriodType periodType = rankingRequestValidator.parseAndValidatePeriod(dto.getPeriodType(), dto.getPeriodValue());
        Dog dog = accessValidator.getDogOrThrow(userId);
        Long regionId = dto.getRegionId();

        List<PersonalRankItemResponse> topRanks;
        PersonalRankItemResponse myRank;
        if (regionId == null) {
            topRanks = dogGlobalRankRepository
                    .findRanks(periodType, dto.getPeriodValue(), PageRequest.of(0, SUMMARY_TOP_LIMIT))
                    .stream()
                    .map(this::toPersonalRankItem)
                    .toList();

            myRank = dogGlobalRankRepository
                    .findMyRank(periodType, dto.getPeriodValue(), dog.getId())
                    .map(this::toPersonalRankItem)
                    .orElse(null);
        } else {
            regionValidator.validateActiveRegion(regionId);
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
        rankingRequestValidator.validateRequestNotNull(dto);
        RankingPeriodType periodType = rankingRequestValidator.parseAndValidatePeriod(dto.getPeriodType(), dto.getPeriodValue());
        accessValidator.getDogOrThrow(userId);

        Long regionId = dto.getRegionId();
        if (regionId != null) {
            regionValidator.validateActiveRegion(regionId);
        }
        String cursor = rankingRequestValidator.resolveCursor(cursorRequest);
        int limit = rankingRequestValidator.resolveLimit(cursorRequest);

        CursorPagingSupport.CursorPageResult<PersonalRankItemResponse> page = cursorPagingSupport.paginate(
                cursor,
                limit,
                fetchSize -> regionId == null
                        ? dogGlobalRankRepository.findRanks(periodType, dto.getPeriodValue(), PageRequest.of(0, fetchSize))
                        : dogRankRepository.findRanks(periodType, dto.getPeriodValue(), regionId, PageRequest.of(0, fetchSize)),
                RankingValidator::parseRankDogCursor,
                (parsedCursor, pageLimit) -> regionId == null
                        ? dogGlobalRankRepository.findRanksByCursor(
                        periodType,
                        dto.getPeriodValue(),
                        parsedCursor.rank(),
                        parsedCursor.dogId(),
                        PageRequest.of(0, pageLimit)
                )
                        : dogRankRepository.findRanksByCursor(
                        periodType,
                        dto.getPeriodValue(),
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
}
