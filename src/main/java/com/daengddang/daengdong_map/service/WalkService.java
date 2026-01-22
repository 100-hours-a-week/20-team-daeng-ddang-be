package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.BlockIdUtil;
import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.block.Block;
import com.daengddang.daengdong_map.domain.block.BlockOwnership;
import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.domain.walk.Walk;
import com.daengddang.daengdong_map.domain.walk.WalkPoint;
import com.daengddang.daengdong_map.domain.walk.WalkStatus;
import com.daengddang.daengdong_map.dto.request.walk.WalkEndRequest;
import com.daengddang.daengdong_map.dto.request.walk.WalkStartRequest;
import com.daengddang.daengdong_map.dto.response.walk.OccupiedBlockListResponse;
import com.daengddang.daengdong_map.dto.response.walk.OccupiedBlockResponse;
import com.daengddang.daengdong_map.dto.response.walk.WalkEndResponse;
import com.daengddang.daengdong_map.dto.response.walk.WalkStartResponse;
import com.daengddang.daengdong_map.repository.BlockOwnershipRepository;
import com.daengddang.daengdong_map.repository.DogRepository;
import com.daengddang.daengdong_map.repository.UserRepository;
import com.daengddang.daengdong_map.repository.WalkPointRepository;
import com.daengddang.daengdong_map.repository.WalkRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalkService {

    private static final double KM_TO_METER = 1000.0;

    private final UserRepository userRepository;
    private final DogRepository dogRepository;
    private final WalkRepository walkRepository;
    private final WalkPointRepository walkPointRepository;
    private final BlockOwnershipRepository blockOwnershipRepository;

    @Transactional
    public WalkStartResponse startWalk(Long userId, WalkStartRequest request) {

        if (request == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        Dog dog = dogRepository.findByUser(user)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND));

        if (walkRepository.existsByDogAndStatus(dog, WalkStatus.IN_PROGRESS)) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        LocalDateTime now = LocalDateTime.now();

        Walk walk = Walk.builder()
                .dog(dog)
                .startTime(now)
                .status(WalkStatus.IN_PROGRESS)
                .build();

        Walk saved = walkRepository.save(walk);

        WalkPoint startPoint = WalkPoint.builder()
                .walk(saved)
                .latitude(request.getStartLat())
                .longitude(request.getStartLng())
                .recordedAt(now)
                .build();

        walkPointRepository.save(startPoint);

        return WalkStartResponse.of(saved.getId(), saved.getStartTime());
    }

    @Transactional
    public WalkEndResponse endWalk(Long userId, Long walkId, WalkEndRequest request) {

        if (request == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        Dog dog = dogRepository.findByUser(user)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND));

        Walk walk = walkRepository.findByIdAndDog(walkId, dog)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND));

        if (walk.getStatus() == WalkStatus.FINISHED) {
            throw new BaseException(ErrorCode.WALK_ALREADY_ENDED);
        }

        if (request.getStatus() != WalkStatus.FINISHED) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        if (request.getTotalDistanceKm() < 0 || request.getDurationSeconds() <= 0) {
            throw new BaseException(ErrorCode.INVALID_WALK_METRICS);
        }

        LocalDateTime now = LocalDateTime.now();
        double distanceMeters = request.getTotalDistanceKm() * KM_TO_METER;

        walk.finish(now, distanceMeters, request.getDurationSeconds());

        WalkPoint endPoint = WalkPoint.builder()
                .walk(walk)
                .latitude(request.getEndLat())
                .longitude(request.getEndLng())
                .recordedAt(now)
                .build();
        walkPointRepository.save(endPoint);

        int occupiedBlockCount = blockOwnershipRepository.findAllByDog(dog).size();

        return WalkEndResponse.of(
                walk.getId(),
                walk.getStartTime(),
                walk.getEndTime(),
                request.getTotalDistanceKm(),
                request.getDurationSeconds(),
                occupiedBlockCount,
                walk.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public OccupiedBlockListResponse getOccupiedBlocks(Long userId, Long walkId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        Dog dog = dogRepository.findByUser(user)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND));

        walkRepository.findByIdAndDog(walkId, dog)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND));

        List<OccupiedBlockResponse> blocks = blockOwnershipRepository.findAllByDog(dog).stream()
                .map(this::toOccupiedBlock)
                .toList();

        return OccupiedBlockListResponse.of(blocks);
    }

    private OccupiedBlockResponse toOccupiedBlock(BlockOwnership ownership) {
        Block block = ownership.getBlock();
        String blockId = BlockIdUtil.toBlockId(block.getX(), block.getY());
        return OccupiedBlockResponse.of(
                blockId,
                ownership.getDog().getId(),
                ownership.getAcquiredAt()
        );
    }
}
