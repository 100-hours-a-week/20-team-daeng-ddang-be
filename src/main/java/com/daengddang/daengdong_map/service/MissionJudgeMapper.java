package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.mission.Mission;
import com.daengddang.daengdong_map.dto.request.mission.FastApiMissionJudgeRequest;
import com.daengddang.daengdong_map.dto.request.mission.MissionJudgeRequest;
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

    public FastApiMissionJudgeRequest toFastApiRequest(MissionJudgeRequest request, List<Mission> missions) {
        String analysisId = UUID.randomUUID().toString();
        return FastApiMissionJudgeRequest.of(
                analysisId,
                request.getWalkId(),
                toMissionItems(request, missions)
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
                response.getAnalyzedAt(),
                results
        );
    }

    private List<FastApiMissionJudgeRequest.MissionItem> toMissionItems(
            MissionJudgeRequest request,
            List<Mission> missions
    ) {
        Map<Long, Mission> missionById = missions.stream()
                .collect(Collectors.toMap(Mission::getId, Function.identity()));

        return request.getMissions().stream()
                .map(item -> {
                    Mission mission = missionById.get(item.getMissionId());
                    if (mission == null) {
                        throw new BaseException(ErrorCode.RESOURCE_NOT_FOUND);
                    }
                    return FastApiMissionJudgeRequest.MissionItem.of(
                            mission.getId(),
                            mission.getMissionType(),
                            item.getVideoUrl()
                    );
                })
                .toList();
    }
}
