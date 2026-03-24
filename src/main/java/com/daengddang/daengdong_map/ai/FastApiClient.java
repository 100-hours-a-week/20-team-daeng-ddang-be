package com.daengddang.daengdong_map.ai;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.dto.request.chat.FastApiHealthcareChatRequest;
import com.daengddang.daengdong_map.dto.request.expression.FastApiExpressionAnalyzeRequest;
import com.daengddang.daengdong_map.dto.request.healthcare.FastApiHealthcareAnalyzeRequest;
import com.daengddang.daengdong_map.dto.request.mission.FastApiMissionJudgeRequest;
import com.daengddang.daengdong_map.dto.response.chat.FastApiHealthcareChatResponse;
import com.daengddang.daengdong_map.dto.response.expression.FastApiExpressionAnalyzeResponse;
import com.daengddang.daengdong_map.dto.response.healthcare.FastApiHealthcareAnalyzeResponse;
import com.daengddang.daengdong_map.dto.response.mission.FastApiMissionJudgeResponse;
import io.github.resilience4j.timelimiter.TimeLimiter;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.ConnectException;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class FastApiClient {

    @Qualifier("fastApiRestClient")
    private final RestClient restClient;
    private final FastApiProperties fastApiProperties;
    @Qualifier("missionFastApiTimeLimiter")
    private final TimeLimiter missionFastApiTimeLimiter;
    @Qualifier("expressionFastApiTimeLimiter")
    private final TimeLimiter expressionFastApiTimeLimiter;
    @Qualifier("healthcareFastApiTimeLimiter")
    private final TimeLimiter healthcareFastApiTimeLimiter;
    @Qualifier("chatFastApiTimeLimiter")
    private final TimeLimiter chatFastApiTimeLimiter;
    private final FastApiCallExecutor fastApiCallExecutor;

    public FastApiMissionJudgeResponse requestMissionJudge(FastApiMissionJudgeRequest request) {
        Instant startedAt = Instant.now();
        try {
            log.info("FastAPI mission 요청 시작. analysisId={}, walkId={}, uri={}",
                    request.getAnalysisId(), request.getWalkId(), fastApiProperties.getMissionJudgeUri());
            FastApiMissionJudgeResponse response = fastApiCallExecutor.execute(missionFastApiTimeLimiter, () -> restClient.post()
                    .uri(fastApiProperties.getMissionJudgeUri())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(FastApiMissionJudgeResponse.class));
            log.info("FastAPI mission 요청 성공. analysisId={}, walkId={}, uri={}, durationMs={}",
                    request.getAnalysisId(), request.getWalkId(), fastApiProperties.getMissionJudgeUri(),
                    Duration.between(startedAt, Instant.now()).toMillis());
            return response;
        } catch (Exception e) {
            log.warn("FastAPI mission 요청 실패. analysisId={}, walkId={}, uri={}, durationMs={}, errorType={}, message={}",
                    request.getAnalysisId(), request.getWalkId(), fastApiProperties.getMissionJudgeUri(),
                    Duration.between(startedAt, Instant.now()).toMillis(),
                    e.getClass().getSimpleName(), e.getMessage(), e);
            throw mapFastApiException(e);
        }
    }

    public FastApiExpressionAnalyzeResponse requestExpressionAnalyze(FastApiExpressionAnalyzeRequest request) {
        Instant startedAt = Instant.now();
        try {
            log.info("FastAPI expression 요청 시작. analysisId={}, videoUrl={}, uri={}",
                    request.getAnalysisId(), request.getVideoUrl(), fastApiProperties.getExpressionAnalyzeUri());
            FastApiExpressionAnalyzeResponse response = fastApiCallExecutor.execute(expressionFastApiTimeLimiter, () -> restClient.post()
                    .uri(fastApiProperties.getExpressionAnalyzeUri())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(FastApiExpressionAnalyzeResponse.class));
            log.info("FastAPI expression 요청 성공. analysisId={}, uri={}, durationMs={}",
                    request.getAnalysisId(), fastApiProperties.getExpressionAnalyzeUri(),
                    Duration.between(startedAt, Instant.now()).toMillis());
            return response;
        } catch (Exception e) {
            log.warn("FastAPI expression 요청 실패. analysisId={}, uri={}, durationMs={}, errorType={}, message={}",
                    request.getAnalysisId(), fastApiProperties.getExpressionAnalyzeUri(),
                    Duration.between(startedAt, Instant.now()).toMillis(),
                    e.getClass().getSimpleName(), e.getMessage(), e);
            throw mapFastApiException(e);
        }
    }

    public FastApiHealthcareAnalyzeResponse requestHealthcareAnalyze(FastApiHealthcareAnalyzeRequest request) {
        Instant startedAt = Instant.now();
        try {
            log.info("FastAPI healthcare 요청 시작. analysisId={}, dogId={}, videoUrl={}, uri={}",
                    request.getAnalysisId(), request.getDogId(), request.getVideoUrl(),
                    fastApiProperties.getHealthcareAnalyzeUri());
            FastApiHealthcareAnalyzeResponse response = fastApiCallExecutor.execute(healthcareFastApiTimeLimiter, () -> restClient.post()
                    .uri(fastApiProperties.getHealthcareAnalyzeUri())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(FastApiHealthcareAnalyzeResponse.class));
            log.info("FastAPI healthcare 요청 성공. analysisId={}, dogId={}, uri={}, durationMs={}",
                    request.getAnalysisId(), request.getDogId(), fastApiProperties.getHealthcareAnalyzeUri(),
                    Duration.between(startedAt, Instant.now()).toMillis());
            return response;
        } catch (Exception e) {
            log.warn("FastAPI healthcare 요청 실패. analysisId={}, dogId={}, uri={}, durationMs={}, errorType={}, message={}",
                    request.getAnalysisId(), request.getDogId(), fastApiProperties.getHealthcareAnalyzeUri(),
                    Duration.between(startedAt, Instant.now()).toMillis(),
                    e.getClass().getSimpleName(), e.getMessage(), e);
            throw mapFastApiException(e);
        }
    }

    public FastApiHealthcareChatResponse requestHealthcareChat(FastApiHealthcareChatRequest request) {
        Instant startedAt = Instant.now();
        try {
            log.info("FastAPI chat 요청 시작. conversationId={}, uri={}",
                    request.getConversationId(), fastApiProperties.getHealthcareChatUri());
            FastApiHealthcareChatResponse response = fastApiCallExecutor.execute(chatFastApiTimeLimiter, () -> restClient.post()
                    .uri(fastApiProperties.getHealthcareChatUri())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(FastApiHealthcareChatResponse.class));
            log.info("FastAPI chat 요청 성공. conversationId={}, uri={}, durationMs={}",
                    request.getConversationId(), fastApiProperties.getHealthcareChatUri(),
                    Duration.between(startedAt, Instant.now()).toMillis());
            return response;
        } catch (Exception e) {
            log.warn("FastAPI chat 요청 실패. conversationId={}, uri={}, durationMs={}, errorType={}, message={}",
                    request.getConversationId(), fastApiProperties.getHealthcareChatUri(),
                    Duration.between(startedAt, Instant.now()).toMillis(),
                    e.getClass().getSimpleName(), e.getMessage(), e);
            throw mapFastApiException(e);
        }
    }

    private BaseException mapFastApiException(Exception exception) {
        if (isTimeoutException(exception)) {
            return new BaseException(ErrorCode.AI_SERVER_TIMEOUT, exception);
        }
        if (exception instanceof HttpServerErrorException) {
            return new BaseException(ErrorCode.AI_SERVER_INTERNAL_ERROR, exception);
        }
        if (exception instanceof HttpClientErrorException) {
            return new BaseException(ErrorCode.AI_SERVER_BAD_REQUEST, exception);
        }
        if (exception instanceof BaseException baseException
                && (baseException.getErrorCode() == ErrorCode.AI_SERVER_BULKHEAD_REJECTED
                || baseException.getErrorCode() == ErrorCode.AI_SERVER_CIRCUIT_OPEN)) {
            return baseException;
        }
        if (isDeserializeException(exception)) {
            return new BaseException(ErrorCode.AI_SERVER_RESPONSE_INVALID, exception);
        }
        if (isConnectionException(exception)) {
            return new BaseException(ErrorCode.AI_SERVER_CONNECTION_FAILED, exception);
        }
        return new BaseException(ErrorCode.AI_SERVER_CONNECTION_FAILED, exception);
    }

    private boolean isTimeoutException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof HttpTimeoutException
                    || current instanceof HttpConnectTimeoutException
                    || current instanceof TimeoutException) {
                return true;
            }
            if (current instanceof ConnectException) {
                return false;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isDeserializeException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof HttpMessageNotReadableException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isConnectionException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ConnectException) {
                return true;
            }
            if (current instanceof ResourceAccessException) {
                return true;
            }
            if (current instanceof RestClientException && current.getCause() == null) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
