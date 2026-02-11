package com.daengddang.daengdong_map.controller;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.SuccessCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.controller.api.RegionRankingApi;
import com.daengddang.daengdong_map.dto.request.ranking.RankingCursorRequest;
import com.daengddang.daengdong_map.dto.request.ranking.RankingPeriodRequest;
import com.daengddang.daengdong_map.dto.request.ranking.RankingPeriodRegionRequest;
import com.daengddang.daengdong_map.dto.response.ranking.region.RegionRankingListResponse;
import com.daengddang.daengdong_map.dto.response.ranking.region.RegionRankingSummaryResponse;
import com.daengddang.daengdong_map.security.AuthUser;
import com.daengddang.daengdong_map.service.ranking.RegionRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v3/rankings/regions")
@RequiredArgsConstructor
public class RegionRankingController implements RegionRankingApi {

    private final RegionRankingService regionRankingService;

    @GetMapping("/summary")
    @Override
    public ApiResponse<RegionRankingSummaryResponse> getRegionRankingSummary(
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

        RegionRankingSummaryResponse response = regionRankingService
                .getRegionRankingSummary(authUser.getUserId(), dto);

        return ApiResponse.success(SuccessCode.REGION_RANKING_SUMMARY_RETRIEVED, response);
    }

    @GetMapping
    @Override
    public ApiResponse<RegionRankingListResponse> getRegionRankingList(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam String periodType,
            @RequestParam String periodValue,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit
    ) {
        if (authUser == null) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        RankingPeriodRequest dto = RankingPeriodRequest.builder()
                .periodType(periodType)
                .periodValue(periodValue)
                .build();

        RankingCursorRequest cursorDto = RankingCursorRequest.builder()
                .cursor(cursor)
                .limit(limit)
                .build();

        RegionRankingListResponse response = regionRankingService
                .getRegionRankingList(authUser.getUserId(), dto, cursorDto);

        return ApiResponse.success(SuccessCode.REGION_RANKING_LIST_RETRIEVED, response);
    }
}
