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
import lombok.RequiredArgsConstructor;
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
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

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
    private final ExecutorService fastApiExecutorService;

    public FastApiMissionJudgeResponse requestMissionJudge(FastApiMissionJudgeRequest request) {
        try {
            return executeWithHardTimeout(missionFastApiTimeLimiter, () -> restClient.post()
                    .uri(fastApiProperties.getMissionJudgeUri())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(FastApiMissionJudgeResponse.class));
        } catch (Exception e) {
            throw mapFastApiException(e);
        }
    }

    public FastApiExpressionAnalyzeResponse requestExpressionAnalyze(FastApiExpressionAnalyzeRequest request) {
        try {
            return executeWithHardTimeout(expressionFastApiTimeLimiter, () -> restClient.post()
                    .uri(fastApiProperties.getExpressionAnalyzeUri())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(FastApiExpressionAnalyzeResponse.class));
        } catch (Exception e) {
            throw mapFastApiException(e);
        }
    }

    public FastApiHealthcareAnalyzeResponse requestHealthcareAnalyze(FastApiHealthcareAnalyzeRequest request) {
        try {
            return executeWithHardTimeout(healthcareFastApiTimeLimiter, () -> restClient.post()
                    .uri(fastApiProperties.getHealthcareAnalyzeUri())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(FastApiHealthcareAnalyzeResponse.class));
        } catch (Exception e) {
            throw mapFastApiException(e);
        }
    }

    public FastApiHealthcareChatResponse requestHealthcareChat(FastApiHealthcareChatRequest request) {
        try {
            return executeWithHardTimeout(chatFastApiTimeLimiter, () -> restClient.post()
                    .uri(fastApiProperties.getHealthcareChatUri())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(FastApiHealthcareChatResponse.class));
        } catch (Exception e) {
            throw mapFastApiException(e);
        }
    }

    private <T> T executeWithHardTimeout(TimeLimiter timeLimiter, Supplier<T> supplier) throws Exception {
        Future<T> future = fastApiExecutorService.submit(supplier::get);
        try {
            return timeLimiter.executeFutureSupplier(() -> future);
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof Exception exception) {
                throw exception;
            }
            throw ex;
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
