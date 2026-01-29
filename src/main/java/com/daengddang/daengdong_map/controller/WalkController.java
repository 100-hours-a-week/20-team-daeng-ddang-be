package com.daengddang.daengdong_map.controller;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.SuccessCode;
import com.daengddang.daengdong_map.dto.request.diaries.WalkDiariesCreateRequest;
import com.daengddang.daengdong_map.dto.request.walk.WalkEndRequest;
import com.daengddang.daengdong_map.dto.request.walk.WalkStartRequest;
import com.daengddang.daengdong_map.dto.response.diaries.WalkDiariesCreateResponse;
import com.daengddang.daengdong_map.dto.response.walk.OccupiedBlockListResponse;
import com.daengddang.daengdong_map.dto.response.walk.WalkEndResponse;
import com.daengddang.daengdong_map.dto.response.walk.WalkStartResponse;
import com.daengddang.daengdong_map.security.AuthUser;
import com.daengddang.daengdong_map.service.WalkDiaryService;
import com.daengddang.daengdong_map.service.WalkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v3/walks")
@RequiredArgsConstructor
public class WalkController {

    private final WalkService walkService;
    private final WalkDiaryService walkDiaryService;

    @PostMapping
    public ApiResponse<WalkStartResponse> startWalk(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody WalkStartRequest dto
    ) {
        WalkStartResponse response = walkService.startWalk(authUser.getUserId(), dto);
        return ApiResponse.success(SuccessCode.WALK_STARTED, response);
    }

    @PostMapping("/{walkId}")
    public ApiResponse<WalkEndResponse> endWalk(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long walkId,
            @Valid @RequestBody WalkEndRequest dto
    ) {
        WalkEndResponse response = walkService.endWalk(authUser.getUserId(), walkId, dto);
        return ApiResponse.success(SuccessCode.WALK_ENDED, response);
    }

    @GetMapping("/{walkId}/blocks")
    public ApiResponse<OccupiedBlockListResponse> getOccupiedBlocks(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long walkId
    ) {
        OccupiedBlockListResponse response = walkService.getOccupiedBlocks(authUser.getUserId(), walkId);
        return ApiResponse.success(SuccessCode.OCCUPIED_BLOCKS_RETRIEVED, response);
    }

    @PostMapping("/{walkId}/diaries")
    public ApiResponse<WalkDiariesCreateResponse> writeWalkDiary(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long walkId,
            @Valid @RequestBody WalkDiariesCreateRequest dto
            ) {
        WalkDiariesCreateResponse response = walkDiaryService.writeWalkDiary(dto, authUser.getUserId(), walkId);
        return ApiResponse.success(SuccessCode.WALK_DIARY_CREATED, response);
    }
}
