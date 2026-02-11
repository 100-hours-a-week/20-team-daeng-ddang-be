package com.daengddang.daengdong_map.controller.api;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.api.ErrorCodes;
import com.daengddang.daengdong_map.dto.response.ranking.contribution.RegionContributionRankingListResponse;
import com.daengddang.daengdong_map.dto.response.ranking.contribution.RegionContributionRankingSummaryResponse;
import com.daengddang.daengdong_map.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Ranking", description = "Ranking endpoints")
public interface RegionContributionRankingApi {

    @Operation(summary = "Get region contribution ranking summary")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INVALID_FORMAT, INVALID_PERIOD_FORMAT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "REGION_NOT_FOUND"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_FORMAT,
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_PERIOD_FORMAT,
            com.daengddang.daengdong_map.common.ErrorCode.REGION_NOT_FOUND
    })
    ApiResponse<RegionContributionRankingSummaryResponse> getRegionContributionRankingSummary(
            @Parameter(hidden = true) AuthUser authUser,
            @RequestParam String periodType,
            @RequestParam String periodValue,
            @RequestParam Long regionId
    );

    @Operation(summary = "Get region contribution ranking list")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INVALID_FORMAT, INVALID_PERIOD_FORMAT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "REGION_NOT_FOUND"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_FORMAT,
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_PERIOD_FORMAT,
            com.daengddang.daengdong_map.common.ErrorCode.REGION_NOT_FOUND
    })
    ApiResponse<RegionContributionRankingListResponse> getRegionContributionRankingList(
            @Parameter(hidden = true) AuthUser authUser,
            @RequestParam String periodType,
            @RequestParam String periodValue,
            @RequestParam Long regionId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit
    );
}
