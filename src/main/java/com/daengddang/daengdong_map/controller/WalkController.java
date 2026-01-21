package com.daengddang.daengdong_map.controller;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.dto.request.walk.WalkEndRequest;
import com.daengddang.daengdong_map.dto.request.walk.WalkStartRequest;
import com.daengddang.daengdong_map.dto.response.walk.OccupiedBlockListResponse;
import com.daengddang.daengdong_map.dto.response.walk.WalkEndResponse;
import com.daengddang.daengdong_map.dto.response.walk.WalkStartResponse;
import com.daengddang.daengdong_map.security.AuthUser;
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

    @PostMapping
    public ApiResponse<WalkStartResponse> startWalk(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody WalkStartRequest request
    ) {
        WalkStartResponse response = walkService.startWalk(authUser.getUserId(), request);
        return ApiResponse.success("산책이 시작되었습니다.", response);
    }

    @PostMapping("/{walkId}")
    public ApiResponse<WalkEndResponse> endWalk(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long walkId,
            @Valid @RequestBody WalkEndRequest request
    ) {
        WalkEndResponse response = walkService.endWalk(authUser.getUserId(), walkId, request);
        return ApiResponse.success("산책이 정상적으로 종료되었습니다.", response);
    }

    @GetMapping("/{walkId}/blocks")
    public ApiResponse<OccupiedBlockListResponse> getOccupiedBlocks(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long walkId
    ) {
        OccupiedBlockListResponse response = walkService.getOccupiedBlocks(authUser.getUserId(), walkId);
        return ApiResponse.success("점유 블록 조회에 성공했습니다.", response);
    }
}
