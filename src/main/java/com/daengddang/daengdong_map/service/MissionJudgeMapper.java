package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.mission.Mission;
import com.daengddang.daengdong_map.domain.mission.MissionUpload;
import com.daengddang.daengdong_map.dto.request.mission.FastApiMissionJudgeRequest;
import com.daengddang.daengdong_map.dto.response.mission.FastApiMissionJudgeResponse;
import com.daengddang.daengdong_map.dto.response.mission.MissionJudgeResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class MissionJudgeMapper {

    public FastApiMissionJudgeRequest toFastApiRequest(
            List<MissionUpload> uploads,
            List<Mission> missions,
            Long walkId
    ) {
        String analysisId = UUID.randomUUID().toString();
        return FastApiMissionJudgeRequest.of(
                analysisId,
                walkId,
                toMissionItems(uploads, missions)
        );
    }

    public MissionJudgeResponse toPublicResponse(FastApiMissionJudgeResponse response) {
        List<MissionJudgeResponse.MissionResult> results = response.getMissions().stream()
                .map(result -> MissionJudgeResponse.MissionResult.from(
                        result.getMissionId(),
                        result.getSuccess()
                ))
                .toList();

        return MissionJudgeResponse.from(
                response.getAnalysisId(),
                response.getWalkId(),
                response.getAnalyzedAt() == null ? null : response.getAnalyzedAt().toLocalDateTime(),
                results
        );
    }

    private List<FastApiMissionJudgeRequest.MissionItem> toMissionItems(
            List<MissionUpload> uploads,
            List<Mission> missions
    ) {
        Map<Long, Mission> missionById = missions.stream()
                .collect(Collectors.toMap(Mission::getId, Function.identity()));

        return uploads.stream()
                .map(upload -> {
                    Mission mission = missionById.get(upload.getMission().getId());
                    if (mission == null) {
                        throw new BaseException(ErrorCode.RESOURCE_NOT_FOUND);
                    }
                    return FastApiMissionJudgeRequest.MissionItem.of(
                            mission.getId(),
                            mission.getMissionType(),
                            upload.getVideoUrl()
                    );
                })
                .toList();
    }
}
