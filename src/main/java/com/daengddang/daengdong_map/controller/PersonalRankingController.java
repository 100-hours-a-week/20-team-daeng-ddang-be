package com.daengddang.daengdong_map.controller;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.SuccessCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.controller.api.PersonalRankingApi;
import com.daengddang.daengdong_map.dto.request.ranking.RankingCursorRequest;
import com.daengddang.daengdong_map.dto.request.ranking.RankingPeriodRegionRequest;
import com.daengddang.daengdong_map.dto.response.ranking.personal.PersonalRankingListResponse;
import com.daengddang.daengdong_map.dto.response.ranking.personal.PersonalRankingSummaryResponse;
import com.daengddang.daengdong_map.security.AuthUser;
import com.daengddang.daengdong_map.service.ranking.PersonalRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v3/rankings/dogs")
@RequiredArgsConstructor
public class PersonalRankingController implements PersonalRankingApi {

    private final PersonalRankingService personalRankingService;

    @GetMapping("/summary")
    @Override
    public ApiResponse<PersonalRankingSummaryResponse> getPersonalRankingSummary(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam String periodType,
            @RequestParam String periodValue,
            @RequestParam(required = false) Long regionId
    ) {
        if (authUser == null) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        RankingPeriodRegionRequest dto = RankingPeriodRegionRequest.builder()
                .periodType(periodType)
                .periodValue(periodValue)
                .regionId(regionId)
                .build();

        PersonalRankingSummaryResponse response = personalRankingService
                .getPersonalRankingSummary(authUser.getUserId(), dto);

        return ApiResponse.success(SuccessCode.PERSONAL_RANKING_SUMMARY_RETRIEVED, response);
    }

    @GetMapping
    @Override
    public ApiResponse<PersonalRankingListResponse> getPersonalRankingList(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam String periodType,
            @RequestParam String periodValue,
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit
    ) {
        if (authUser == null) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        RankingPeriodRegionRequest dto = RankingPeriodRegionRequest.builder()
                .periodType(periodType)
                .periodValue(periodValue)
                .regionId(regionId)
                .build();

        RankingCursorRequest cursorRequest = RankingCursorRequest.builder()
                .cursor(cursor)
                .limit(limit)
                .build();

        PersonalRankingListResponse response = personalRankingService
                .getPersonalRankingList(authUser.getUserId(), dto, cursorRequest);

        return ApiResponse.success(SuccessCode.PERSONAL_RANKING_LIST_RETRIEVED, response);
    }
}
