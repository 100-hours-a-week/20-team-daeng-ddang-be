package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.ai.FastApiClient;
import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.mission.Mission;
import com.daengddang.daengdong_map.domain.mission.MissionRecord;
import com.daengddang.daengdong_map.domain.mission.MissionRecordStatus;
import com.daengddang.daengdong_map.domain.mission.MissionUpload;
import com.daengddang.daengdong_map.domain.walk.Walk;
import com.daengddang.daengdong_map.domain.walk.WalkStatus;
import com.daengddang.daengdong_map.dto.request.mission.FastApiMissionJudgeRequest;
import com.daengddang.daengdong_map.dto.response.mission.FastApiMissionJudgeResponse;
import com.daengddang.daengdong_map.dto.response.mission.MissionJudgeResponse;
import com.daengddang.daengdong_map.util.MissionJudgeMapper;
import com.daengddang.daengdong_map.repository.MissionRepository;
import com.daengddang.daengdong_map.repository.MissionRecordRepository;
import com.daengddang.daengdong_map.repository.MissionUploadRepository;
import com.daengddang.daengdong_map.util.AccessValidator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MissionJudgeService {

    private final AccessValidator accessValidator;
    private final MissionRepository missionRepository;
    private final MissionRecordRepository missionRecordRepository;
    private final MissionUploadRepository missionUploadRepository;
    private final MissionJudgeMapper missionJudgeMapper;
    private final FastApiClient fastApiClient;

    @Transactional
    public MissionJudgeResponse judge(Long userId, Long walkId) {
        Walk walk = accessValidator.getOwnedWalkOrThrow(userId, walkId);

        if (walk.getStatus() != WalkStatus.FINISHED) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        List<MissionUpload> uploads = missionUploadRepository.findAllByWalk(walk);
        if (uploads.isEmpty()) {
            throw new BaseException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        List<Long> missionIds = uploads.stream()
                .map(upload -> upload.getMission().getId())
                .distinct()
                .toList();

        List<Mission> missions = missionRepository.findAllById(missionIds);
        if (missions.size() != missionIds.size()) {
            throw new BaseException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        LocalDateTime submittedAt = LocalDateTime.now();
        FastApiMissionJudgeRequest fastApiRequest =
                missionJudgeMapper.toFastApiRequest(uploads, missions, walkId);
        FastApiMissionJudgeResponse fastApiResponse =
                fastApiClient.requestMissionJudge(fastApiRequest);
        MissionJudgeResponse response = missionJudgeMapper.toPublicResponse(fastApiResponse);
        saveMissionRecords(uploads, missions, fastApiResponse, walk, submittedAt);

        return response;
    }

    private void saveMissionRecords(
            List<MissionUpload> uploads,
            List<Mission> missions,
            FastApiMissionJudgeResponse fastApiResponse,
            Walk walk,
            LocalDateTime submittedAt
    ) {
        Map<Long, Mission> missionById = missions.stream()
                .collect(Collectors.toMap(Mission::getId, Function.identity()));
        Map<Long, String> videoUrlByMissionId = uploads.stream()
                .collect(Collectors.toMap(upload -> upload.getMission().getId(),
                        MissionUpload::getVideoUrl));
        Map<Long, Boolean> successByMissionId = fastApiResponse.getMissions().stream()
                .collect(Collectors.toMap(FastApiMissionJudgeResponse.MissionResult::getMissionId,
                        FastApiMissionJudgeResponse.MissionResult::getSuccess));

        LocalDateTime analyzedAt = fastApiResponse.getAnalyzedAt() == null
                ? LocalDateTime.now()
                : fastApiResponse.getAnalyzedAt().toLocalDateTime();

        List<MissionRecord> records = uploads.stream()
                .map(upload -> {
                    Long missionId = upload.getMission().getId();
                    Mission mission = missionById.get(missionId);
                    String videoUrl = videoUrlByMissionId.get(missionId);
                    Boolean success = successByMissionId.get(missionId);
                    if (mission == null || videoUrl == null || success == null) {
                        throw new BaseException(ErrorCode.INVALID_FORMAT);
                    }
                    MissionRecordStatus status = success ? MissionRecordStatus.SUCCESS : MissionRecordStatus.FAIL;
                    return MissionRecord.builder()
                            .mission(mission)
                            .walk(walk)
                            .status(status)
                            .message(null)
                            .missionVideoUrl(videoUrl)
                            .submittedAt(submittedAt)
                            .analyzedAt(analyzedAt)
                            .build();
                })
                .toList();

        missionRecordRepository.saveAll(records);
    }
}
