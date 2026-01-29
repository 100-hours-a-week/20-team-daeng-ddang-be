package com.daengddang.daengdong_map.ai;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.dto.request.expression.FastApiFaceAnalyzeRequest;
import com.daengddang.daengdong_map.dto.request.mission.FastApiMissionJudgeRequest;
import com.daengddang.daengdong_map.dto.response.expression.FastApiFaceAnalyzeResponse;
import com.daengddang.daengdong_map.dto.response.mission.FastApiMissionJudgeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class FastApiClient {

    private final RestClient restClient;
    private final FastApiProperties fastApiProperties;

    public FastApiMissionJudgeResponse requestMissionJudge(FastApiMissionJudgeRequest request) {
        try {
            return restClient.post()
                    .uri(fastApiProperties.getMissionJudgeUri())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(FastApiMissionJudgeResponse.class);
        } catch (Exception e) {
            throw new BaseException(ErrorCode.AI_SERVER_CONNECTION_FAILED);
        }
    }

    public FastApiFaceAnalyzeResponse requestFaceAnalyze(FastApiFaceAnalyzeRequest request) {
        try {
            return restClient.post()
                    .uri(fastApiProperties.getFaceAnalyzeUri())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(FastApiFaceAnalyzeResponse.class);
        } catch (Exception e) {
            throw new BaseException(ErrorCode.AI_SERVER_CONNECTION_FAILED);
        }
    }
}
