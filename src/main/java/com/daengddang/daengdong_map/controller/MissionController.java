package com.daengddang.daengdong_map.controller;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.SuccessCode;
import com.daengddang.daengdong_map.dto.response.mission.MissionJudgeResponse;
import com.daengddang.daengdong_map.dto.request.mission.MissionUploadRequest;
import com.daengddang.daengdong_map.dto.response.mission.MissionUploadListResponse;
import com.daengddang.daengdong_map.dto.response.mission.MissionUploadResponse;
import com.daengddang.daengdong_map.security.AuthUser;
import com.daengddang.daengdong_map.service.MissionJudgeService;
import com.daengddang.daengdong_map.service.MissionUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController

@RequestMapping("/api/v3/walks/{walkId}/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionJudgeService missionJudgeService;
    private final MissionUploadService missionUploadService;

    @PostMapping("/analysis")
    public ApiResponse<MissionJudgeResponse> judgeMissions(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long walkId
    ) {

        MissionJudgeResponse response =
                missionJudgeService.judge(authUser.getUserId(), walkId);
        return ApiResponse.success(SuccessCode.MISSION_ANALYSIS_COMPLETED, response);
    }

    @PostMapping
    public ApiResponse<MissionUploadResponse> saveUpload(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long walkId,
            @Valid @RequestBody MissionUploadRequest request
    ) {
        MissionUploadResponse response =
                missionUploadService.saveUpload(authUser.getUserId(), walkId, request);
        return ApiResponse.success(SuccessCode.MISSION_UPLOAD_SAVED, response);
    }

    @GetMapping
    public ApiResponse<MissionUploadListResponse> getUploads(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long walkId
    ) {
        MissionUploadListResponse response = missionUploadService.getUploads(authUser.getUserId(), walkId);
        return ApiResponse.success(SuccessCode.MISSION_UPLOAD_LIST_RETRIEVED, response);
    }
}
