package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.region.Region;
import com.daengddang.daengdong_map.domain.region.RegionStatus;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.dto.request.user.UserRegisterRequest;
import com.daengddang.daengdong_map.dto.request.user.UserUpdateRequest;
import com.daengddang.daengdong_map.dto.response.user.UserInfoResponse;
import com.daengddang.daengdong_map.dto.response.user.UserRegisterResponse;
import com.daengddang.daengdong_map.repository.RegionRepository;
import com.daengddang.daengdong_map.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RegionRepository regionRepository;

    @Transactional
    public UserRegisterResponse registerUserInfo(Long userId, UserRegisterRequest request) {
        if (request == null || request.getRegionId() == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        Region region = regionRepository.findByIdAndStatus(request.getRegionId(), RegionStatus.ACTIVE)
                .orElseThrow(() -> new BaseException(ErrorCode.REGION_NOT_FOUND));

        user.updateRegion(region);

        return UserRegisterResponse.of(user.getId(), region.getId());
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        return UserInfoResponse.from(user);
    }

    @Transactional
    public UserInfoResponse updateUserInfo(Long userId, UserUpdateRequest request) {
        if (request == null || request.getRegionId() == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));
        Region region = regionRepository.findByIdAndStatus(request.getRegionId(), RegionStatus.ACTIVE)
                .orElseThrow(() -> new BaseException(ErrorCode.REGION_NOT_FOUND));

        user.updateRegion(region);

        return UserInfoResponse.from(user);
    }
}
