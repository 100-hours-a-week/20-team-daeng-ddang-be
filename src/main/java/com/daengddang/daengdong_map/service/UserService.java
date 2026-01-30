package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.region.Region;
import com.daengddang.daengdong_map.domain.region.RegionStatus;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.domain.walk.WalkStatus;
import com.daengddang.daengdong_map.dto.request.user.UserRegisterRequest;
import com.daengddang.daengdong_map.dto.request.user.UserUpdateRequest;
import com.daengddang.daengdong_map.dto.response.user.UserSummaryResponse;
import com.daengddang.daengdong_map.dto.response.user.UserInfoResponse;
import com.daengddang.daengdong_map.dto.response.user.UserRegisterResponse;
import com.daengddang.daengdong_map.repository.DogRepository;
import com.daengddang.daengdong_map.repository.RefreshTokenRepository;
import com.daengddang.daengdong_map.repository.RegionRepository;
import com.daengddang.daengdong_map.repository.UserRepository;
import com.daengddang.daengdong_map.repository.WalkRepository;
import com.daengddang.daengdong_map.repository.WalkSummary;
import com.daengddang.daengdong_map.util.AccessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final DogRepository dogRepository;
    private final WalkRepository walkRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccessValidator accessValidator;

    @Transactional
    public UserRegisterResponse registerUserInfo(Long userId, UserRegisterRequest dto) {
        if (dto == null || dto.getRegionId() == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        User user = accessValidator.getUserOrThrow(userId);

        Region region = regionRepository.findByIdAndStatus(dto.getRegionId(), RegionStatus.ACTIVE)
                .orElseThrow(() -> new BaseException(ErrorCode.REGION_NOT_FOUND));

        UserRegisterRequest.of(dto, user, region);

        return UserRegisterResponse.from(user.getId(), region.getId());
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(Long userId) {
        User user = accessValidator.getUserOrThrow(userId);

        return UserInfoResponse.from(user);
    }

    @Transactional
    public UserInfoResponse updateUserInfo(Long userId, UserUpdateRequest request) {
        if (request == null || request.getRegionId() == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        User user = accessValidator.getUserOrThrow(userId);
        Region region = regionRepository.findByIdAndStatus(request.getRegionId(), RegionStatus.ACTIVE)
                .orElseThrow(() -> new BaseException(ErrorCode.REGION_NOT_FOUND));

        user.updateRegion(region);

        return UserInfoResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserSummaryResponse getMyPageSummary(Long userId) {
        User user = userRepository.findByIdWithRegion(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        String regionName = user.getRegion() == null ? null : user.getRegion().getFullName();

        Dog dog = dogRepository.findByUser(user).orElse(null);
        if (dog == null) {
            return UserSummaryResponse.of(regionName, null, null, null, 0, 0.0);
        }

        WalkSummary summary = walkRepository.findSummaryByDogAndStatus(dog, WalkStatus.FINISHED);
        long totalCount = summary == null || summary.getTotalCount() == null ? 0L : summary.getTotalCount();
        double totalDistanceMeters = summary == null || summary.getTotalDistance() == null ? 0.0 : summary.getTotalDistance();
        double totalDistanceKm = totalDistanceMeters / 1000.0;

        return UserSummaryResponse.of(
                regionName,
                dog.getId(),
                dog.getProfileImageUrl(),
                dog.getName(),
                Math.toIntExact(totalCount),
                totalDistanceKm
        );
    }

    @Transactional
    public void withdrawUser(Long userId) {
        User user = accessValidator.getUserOrThrow(userId);

        Dog dog = dogRepository.findByUser(user).orElse(null);
        if (dog != null) {
            dogRepository.delete(dog);
        }

        user.updateKakaoEmail(null);
        userRepository.saveAndFlush(user);
        userRepository.delete(user);
        refreshTokenRepository.deleteAllByUserId(userId);
    }
}
