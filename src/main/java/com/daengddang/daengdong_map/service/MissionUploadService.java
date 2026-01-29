package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.mission.Mission;
import com.daengddang.daengdong_map.domain.mission.MissionUpload;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.domain.walk.Walk;
import com.daengddang.daengdong_map.domain.walk.WalkStatus;
import com.daengddang.daengdong_map.dto.request.mission.MissionUploadRequest;
import com.daengddang.daengdong_map.dto.response.mission.MissionUploadListResponse;
import com.daengddang.daengdong_map.dto.response.mission.MissionUploadResponse;
import com.daengddang.daengdong_map.repository.DogRepository;
import com.daengddang.daengdong_map.repository.MissionRepository;
import com.daengddang.daengdong_map.repository.MissionUploadRepository;
import com.daengddang.daengdong_map.repository.UserRepository;
import com.daengddang.daengdong_map.repository.WalkRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MissionUploadService {

    private final UserRepository userRepository;
    private final DogRepository dogRepository;
    private final WalkRepository walkRepository;
    private final MissionRepository missionRepository;
    private final MissionUploadRepository missionUploadRepository;

    @Transactional
    public MissionUploadResponse saveUpload(Long userId, Long walkId, MissionUploadRequest request) {
        if (request == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        Walk walk = findOwnedWalk(userId, walkId);
        if (walk.getStatus() != WalkStatus.IN_PROGRESS) {
            throw new BaseException(ErrorCode.WALK_ALREADY_ENDED);
        }

        Mission mission = missionRepository.findById(request.getMissionId())
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND));

        MissionUpload upload = missionUploadRepository.findByWalkAndMission(walk, mission)
                .orElseGet(() -> MissionUpload.builder()
                        .mission(mission)
                        .walk(walk)
                        .videoUrl(request.getVideoUrl())
                        .build());

        upload.updateVideoUrl(request.getVideoUrl());
        MissionUpload saved = missionUploadRepository.save(upload);

        return MissionUploadResponse.from(
                saved.getId(),
                walkId,
                mission.getId(),
                saved.getVideoUrl(),
                saved.getUploadedAt()
        );
    }

    @Transactional(readOnly = true)
    public MissionUploadListResponse getUploads(Long userId, Long walkId) {
        Walk walk = findOwnedWalk(userId, walkId);

        List<MissionUploadResponse> uploads = missionUploadRepository.findAllByWalk(walk).stream()
                .map(upload -> MissionUploadResponse.from(
                        upload.getId(),
                        walkId,
                        upload.getMission().getId(),
                        upload.getVideoUrl(),
                        upload.getUploadedAt()
                ))
                .toList();

        return MissionUploadListResponse.from(uploads);
    }

    private Walk findOwnedWalk(Long userId, Long walkId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        Dog dog = dogRepository.findByUser(user)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND));

        return walkRepository.findByIdAndDog(walkId, dog)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}
