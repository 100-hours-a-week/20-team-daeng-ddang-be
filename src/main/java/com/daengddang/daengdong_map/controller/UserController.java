package com.daengddang.daengdong_map.controller;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.SuccessCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.dto.request.dog.DogRegisterRequest;
import com.daengddang.daengdong_map.dto.request.dog.DogUpdateRequest;
import com.daengddang.daengdong_map.dto.request.user.UserRegisterRequest;
import com.daengddang.daengdong_map.dto.request.user.UserUpdateRequest;
import com.daengddang.daengdong_map.dto.response.dog.DogRegisterResponse;
import com.daengddang.daengdong_map.dto.response.dog.DogResponse;
import com.daengddang.daengdong_map.dto.response.user.RegionListResponse;
import com.daengddang.daengdong_map.dto.response.user.UserInfoResponse;
import com.daengddang.daengdong_map.dto.response.user.UserRegisterResponse;
import com.daengddang.daengdong_map.dto.response.user.UserSummaryResponse;
import com.daengddang.daengdong_map.security.AuthUser;
import com.daengddang.daengdong_map.service.DogService;
import com.daengddang.daengdong_map.service.RegionService;
import com.daengddang.daengdong_map.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v3/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RegionService regionService;
    private final DogService dogService;

    @PostMapping
    public ApiResponse<UserRegisterResponse> registerUserInfo(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UserRegisterRequest dto
    ) {
        if (authUser == null) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }
        return ApiResponse.success(
                SuccessCode.USER_INFO_REGISTERED,
                userService.registerUserInfo(authUser.getUserId(), dto)
        );
    }

    @GetMapping
    public ApiResponse<UserSummaryResponse> getUserSummary(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        UserSummaryResponse response = userService.getMyPageSummary(authUser.getUserId());
        return ApiResponse.success(SuccessCode.MY_PAGE_SUMMARY_RETRIEVED, response);
    }

    @PatchMapping
    public ApiResponse<UserInfoResponse> updateUserInfo(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UserUpdateRequest dto
    ) {
        if (authUser == null) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }
        return ApiResponse.success(
                SuccessCode.USER_INFO_UPDATED,
                userService.updateUserInfo(authUser.getUserId(), dto)
        );
    }

    @GetMapping("/regions")
    public ApiResponse<RegionListResponse> getRegions(
            @RequestParam(name = "parentId", required = false) Long parentId
    ) {
        return ApiResponse.success(
                SuccessCode.REGION_LIST_RETRIEVED,
                regionService.getRegions(parentId)
        );
    }

    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> getUserInfo(@AuthenticationPrincipal AuthUser authUser) {
        if (authUser == null) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        return ApiResponse.success(SuccessCode.USER_INFO_RETRIEVED, userService.getUserInfo(authUser.getUserId()));
    }

    @DeleteMapping
    public ApiResponse<Void> withdrawUser(@AuthenticationPrincipal AuthUser authUser) {
        if (authUser == null) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        userService.withdrawUser(authUser.getUserId());
        return ApiResponse.success(SuccessCode.USER_DELETED);
    }

    @PostMapping("/dogs")
    public ApiResponse<DogRegisterResponse> registerDog(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody DogRegisterRequest dto
    ) {
        if (authUser == null) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }
        return ApiResponse.success(
                SuccessCode.DOG_REGISTERED,
                dogService.registerDog(authUser.getUserId(), dto)
        );
    }

    @GetMapping("/dogs")
    public ApiResponse<DogResponse> getDogInfo(@AuthenticationPrincipal AuthUser authUser) {
        if (authUser == null) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        return ApiResponse.success(SuccessCode.DOG_INFO_RETRIEVED, dogService.getDogInfo(authUser.getUserId()));
    }

    @PatchMapping("/dogs")
    public ApiResponse<DogResponse> updateDogInfo(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody DogUpdateRequest dto
    ) {
        if (authUser == null) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        return ApiResponse.success(
                SuccessCode.DOG_INFO_UPDATED,
                dogService.updateDogInfo(authUser.getUserId(), dto)
        );
    }
}
