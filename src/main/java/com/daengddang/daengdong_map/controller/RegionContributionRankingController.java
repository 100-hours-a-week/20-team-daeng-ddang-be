package com.daengddang.daengdong_map.controller;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.SuccessCode;
import com.daengddang.daengdong_map.controller.api.RegionContributionRankingApi;
import com.daengddang.daengdong_map.dto.request.ranking.RankingCursorRequest;
import com.daengddang.daengdong_map.dto.request.ranking.RankingPeriodRegionRequest;
import com.daengddang.daengdong_map.dto.response.ranking.contribution.RegionContributionRankingListResponse;
import com.daengddang.daengdong_map.dto.response.ranking.contribution.RegionContributionRankingSummaryResponse;
import com.daengddang.daengdong_map.security.AuthUser;
import com.daengddang.daengdong_map.service.ranking.RegionContributionRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v3/rankings/regions/contributions")
@RequiredArgsConstructor
public class RegionContributionRankingController implements RegionContributionRankingApi {

    private final RegionContributionRankingService regionContributionRankingService;

    @GetMapping("/summary")
    @Override
    public ApiResponse<RegionContributionRankingSummaryResponse> getRegionContributionRankingSummary(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam String periodType,
            @RequestParam String periodValue,
            @RequestParam Long regionId
    ) {
        RankingPeriodRegionRequest dto = RankingPeriodRegionRequest.builder()
                .periodType(periodType)
                .periodValue(periodValue)
                .regionId(regionId)
                .build();

        RegionContributionRankingSummaryResponse response = regionContributionRankingService
                .getRegionContributionRankingSummary(authUser != null ? authUser.getUserId() : null, dto);

        return ApiResponse.success(SuccessCode.REGION_CONTRIBUTION_RANKING_SUMMARY_RETRIEVED, response);
    }

    @GetMapping
    @Override
    public ApiResponse<RegionContributionRankingListResponse> getRegionContributionRankingList(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam String periodType,
            @RequestParam String periodValue,
            @RequestParam Long regionId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit
    ) {
        RankingPeriodRegionRequest dto = RankingPeriodRegionRequest.builder()
                .periodType(periodType)
                .periodValue(periodValue)
                .regionId(regionId)
                .build();

        RankingCursorRequest cursorDto = RankingCursorRequest.builder()
                .cursor(cursor)
                .limit(limit)
                .build();

        RegionContributionRankingListResponse response = regionContributionRankingService
                .getRegionContributionRankingList(authUser != null ? authUser.getUserId() : null, dto, cursorDto);

        return ApiResponse.success(SuccessCode.REGION_CONTRIBUTION_RANKING_LIST_RETRIEVED, response);
    }
}
