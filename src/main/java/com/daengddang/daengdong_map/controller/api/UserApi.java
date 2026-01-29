package com.daengddang.daengdong_map.controller.api;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.api.ErrorCodes;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "User", description = "User and dog profile endpoints")
public interface UserApi {

    @Operation(summary = "Register user info")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INVALID_FORMAT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "REGION_NOT_FOUND"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_FORMAT,
            com.daengddang.daengdong_map.common.ErrorCode.REGION_NOT_FOUND
    })
    ApiResponse<UserRegisterResponse> registerUserInfo(
            @Parameter(hidden = true) AuthUser authUser,
            @RequestBody UserRegisterRequest dto
    );

    @Operation(summary = "Get my page summary")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    ApiResponse<UserSummaryResponse> getUserSummary(@Parameter(hidden = true) AuthUser authUser);

    @Operation(summary = "Update user info")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INVALID_FORMAT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "REGION_NOT_FOUND"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_FORMAT,
            com.daengddang.daengdong_map.common.ErrorCode.REGION_NOT_FOUND
    })
    ApiResponse<UserInfoResponse> updateUserInfo(
            @Parameter(hidden = true) AuthUser authUser,
            @RequestBody UserUpdateRequest dto
    );

    @Operation(summary = "Get region list")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "REGION_NOT_FOUND"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.REGION_NOT_FOUND
    })
    ApiResponse<RegionListResponse> getRegions(
            @RequestParam(name = "parentId", required = false) Long parentId
    );

    @Operation(summary = "Get my user info")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    ApiResponse<UserInfoResponse> getUserInfo(@Parameter(hidden = true) AuthUser authUser);

    @Operation(summary = "Withdraw user")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    ApiResponse<Void> withdrawUser(@Parameter(hidden = true) AuthUser authUser);

    @Operation(summary = "Register dog")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INVALID_FORMAT, DOG_BREED_NOT_FOUND"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_FORMAT,
            com.daengddang.daengdong_map.common.ErrorCode.DOG_BREED_NOT_FOUND
    })
    ApiResponse<DogRegisterResponse> registerDog(
            @Parameter(hidden = true) AuthUser authUser,
            @RequestBody DogRegisterRequest dto
    );

    @Operation(summary = "Get dog info")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    ApiResponse<DogResponse> getDogInfo(@Parameter(hidden = true) AuthUser authUser);

    @Operation(summary = "Update dog info")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INVALID_FORMAT, DOG_BREED_NOT_FOUND"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RESOURCE_NOT_FOUND"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes({
            com.daengddang.daengdong_map.common.ErrorCode.INVALID_FORMAT,
            com.daengddang.daengdong_map.common.ErrorCode.DOG_BREED_NOT_FOUND,
            com.daengddang.daengdong_map.common.ErrorCode.RESOURCE_NOT_FOUND
    })
    ApiResponse<DogResponse> updateDogInfo(
            @Parameter(hidden = true) AuthUser authUser,
            @RequestBody DogUpdateRequest dto
    );
}
