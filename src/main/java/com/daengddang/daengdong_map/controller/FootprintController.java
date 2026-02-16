package com.daengddang.daengdong_map.controller;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.SuccessCode;
import com.daengddang.daengdong_map.controller.api.FootprintApi;
import com.daengddang.daengdong_map.dto.request.footprint.FootprintCalendarRequest;
import com.daengddang.daengdong_map.dto.response.footprint.FootprintCalendarResponse;
import com.daengddang.daengdong_map.dto.response.footprint.FootprintDiaryDetailResponse;
import com.daengddang.daengdong_map.dto.response.footprint.FootprintDiaryExpressionResponse;
import com.daengddang.daengdong_map.dto.response.footprint.FootprintDailyRecordsResponse;
import com.daengddang.daengdong_map.security.AuthUser;
import com.daengddang.daengdong_map.service.FootprintService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v3/footprints")
@RequiredArgsConstructor
public class FootprintController implements FootprintApi {

    private final FootprintService footprintService;

    @GetMapping
    @Override
    public ApiResponse<FootprintCalendarResponse> getCalendarRecords(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam @Min(1900) @Max(2100) Integer year,
            @RequestParam @Min(1) @Max(12) Integer month
    ) {
        FootprintCalendarRequest request = FootprintCalendarRequest.builder()
                .year(year)
                .month(month)
                .build();
        FootprintCalendarResponse response = footprintService.getCalendarRecords(authUser.getUserId(), request);
        return ApiResponse.success(SuccessCode.CALENDAR_RECORD_LIST_RETRIEVED, response);
    }

    @GetMapping("/dates/{date}")
    @Override
    public ApiResponse<FootprintDailyRecordsResponse> getDailyRecords(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable String date
    ) {
        FootprintDailyRecordsResponse response = footprintService.getDailyRecords(authUser.getUserId(), date);
        return ApiResponse.success(SuccessCode.DAILY_RECORD_LIST_RETRIEVED, response);
    }

    @GetMapping("/diaries/{walkDiaryId}/expressions")
    @Override
    public ApiResponse<FootprintDiaryExpressionResponse> getDiaryExpression(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long walkDiaryId
    ) {
        FootprintDiaryExpressionResponse response = footprintService.getDiaryExpression(authUser.getUserId(), walkDiaryId);
        return ApiResponse.success(SuccessCode.EMOTION_ANALYSIS_RESULT_RETRIEVED, response);
    }

    @GetMapping("/diaries/{walkDiaryId}")
    @Override
    public ApiResponse<FootprintDiaryDetailResponse> getDiaryDetail(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long walkDiaryId
    ) {
        FootprintDiaryDetailResponse response = footprintService.getDiaryDetail(authUser.getUserId(), walkDiaryId);
        return ApiResponse.success(SuccessCode.WALK_DIARY_RETRIEVED, response);
    }
}
