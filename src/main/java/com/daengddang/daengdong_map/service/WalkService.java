package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.BlockIdUtil;
import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.block.Block;
import com.daengddang.daengdong_map.domain.block.BlockOwnership;
import com.daengddang.daengdong_map.domain.dog.Dog;
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
import com.daengddang.daengdong_map.util.AccessValidator;
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

    private final WalkRepository walkRepository;
    private final WalkPointRepository walkPointRepository;
    private final BlockOwnershipRepository blockOwnershipRepository;
    private final AccessValidator accessValidator;

    @Transactional
    public WalkStartResponse startWalk(Long userId, WalkStartRequest dto) {

        if (dto == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        Dog dog = accessValidator.getDogOrThrow(userId);

        if (walkRepository.existsByDogAndStatus(dog, WalkStatus.IN_PROGRESS)) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        LocalDateTime now = LocalDateTime.now();

        Walk walk = WalkStartRequest.of(dog, now);

        Walk saved = walkRepository.save(walk);

        WalkPoint startPoint = WalkStartRequest.of(dto, saved, now);

        walkPointRepository.save(startPoint);

        return WalkStartResponse.from(saved.getId(), saved.getStartedAt(), saved.getStatus());
    }

    @Transactional
    public WalkEndResponse endWalk(Long userId, Long walkId, WalkEndRequest dto) {

        if (dto == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        Walk walk = accessValidator.getOwnedWalkOrThrow(userId, walkId);
        Dog dog = walk.getDog();

        if (walk.getStatus() == WalkStatus.FINISHED) {
            throw new BaseException(ErrorCode.WALK_ALREADY_ENDED);
        }

        if (dto.getStatus() != WalkStatus.FINISHED) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        if (dto.getTotalDistanceKm() < 0 || dto.getDurationSeconds() < 0) {
            throw new BaseException(ErrorCode.INVALID_WALK_METRICS);
        }

        LocalDateTime now = LocalDateTime.now();
        double distanceMeters = dto.getTotalDistanceKm() * KM_TO_METER;

        walk.finish(now, distanceMeters, dto.getDurationSeconds());

        WalkPoint endPoint = WalkEndRequest.of(dto, walk, now);
        walkPointRepository.save(endPoint);

        int occupiedBlockCount = blockOwnershipRepository.findAllByDog(dog).size();

        return WalkEndResponse.from(
                walk.getId(),
                walk.getStartedAt(),
                walk.getEndedAt(),
                dto.getTotalDistanceKm(),
                dto.getDurationSeconds(),
                occupiedBlockCount,
                walk.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public OccupiedBlockListResponse getOccupiedBlocks(Long userId, Long walkId) {
        Walk walk = accessValidator.getOwnedWalkOrThrow(userId, walkId);

        List<OccupiedBlockResponse> blocks = blockOwnershipRepository.findAllByDog(walk.getDog()).stream()
                .map(this::toOccupiedBlock)
                .toList();

        return OccupiedBlockListResponse.from(blocks);
    }

    private OccupiedBlockResponse toOccupiedBlock(BlockOwnership ownership) {
        Block block = ownership.getBlock();
        String blockId = BlockIdUtil.toBlockId(block.getX(), block.getY());
        return OccupiedBlockResponse.from(
                blockId,
                ownership.getDog().getId(),
                ownership.getAcquiredAt()
        );
    }
}
