package com.daengddang.daengdong_map.controller;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.SuccessCode;
import com.daengddang.daengdong_map.controller.api.MissionApi;
import com.daengddang.daengdong_map.dto.response.mission.MissionJudgeResponse;
import com.daengddang.daengdong_map.dto.request.mission.MissionUploadRequest;
import com.daengddang.daengdong_map.dto.response.mission.MissionUploadListResponse;
import com.daengddang.daengdong_map.dto.response.mission.MissionUploadResponse;
import com.daengddang.daengdong_map.security.AuthUser;
import com.daengddang.daengdong_map.service.MissionJudgeService;
import com.daengddang.daengdong_map.service.MissionUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController

@RequestMapping("/api/v3/walks/{walkId}/missions")
@RequiredArgsConstructor
public class MissionController implements MissionApi {

    private final MissionJudgeService missionJudgeService;
    private final MissionUploadService missionUploadService;

    @PostMapping("/analysis")
    @Override
    public ApiResponse<MissionJudgeResponse> judgeMissions(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long walkId
    ) {

        MissionJudgeResponse response =
                missionJudgeService.judge(authUser.getUserId(), walkId);
        return ApiResponse.success(SuccessCode.MISSION_ANALYSIS_COMPLETED, response);
    }

    @GetMapping("/analysis")
    @Override
    public ApiResponse<MissionJudgeResponse> getMissionAnalysis(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long walkId
    ) {
        MissionJudgeResponse response =
                missionJudgeService.getResult(authUser.getUserId(), walkId);
        return ApiResponse.success(SuccessCode.MISSION_ANALYSIS_RESULT_RETRIEVED, response);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public ApiResponse<MissionUploadResponse> saveUpload(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long walkId,
            @Valid @RequestBody MissionUploadRequest dto
    ) {
        MissionUploadResponse response =
                missionUploadService.saveUpload(authUser.getUserId(), walkId, dto);
        return ApiResponse.success(SuccessCode.MISSION_UPLOAD_SAVED, response);
    }

    @GetMapping
    @Override
    public ApiResponse<MissionUploadListResponse> getUploads(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long walkId
    ) {
        MissionUploadListResponse response = missionUploadService.getUploads(authUser.getUserId(), walkId);
        return ApiResponse.success(SuccessCode.MISSION_UPLOAD_LIST_RETRIEVED, response);
    }
}
