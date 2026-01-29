package com.daengddang.daengdong_map.controller;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.SuccessCode;
import com.daengddang.daengdong_map.dto.response.mission.MissionListResponse;
import com.daengddang.daengdong_map.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v3/missions")
@RequiredArgsConstructor
public class MissionCatalogController {

    private final MissionService missionService;

    @GetMapping
    public ApiResponse<MissionListResponse> getMissions() {
        MissionListResponse response = missionService.getMissions();
        return ApiResponse.success(SuccessCode.MISSION_LIST_RETRIEVED, response);
    }
}
