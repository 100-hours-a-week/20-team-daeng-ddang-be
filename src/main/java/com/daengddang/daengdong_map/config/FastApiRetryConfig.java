package com.daengddang.daengdong_map.config;

import com.daengddang.daengdong_map.ai.FastApiProperties;
import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.ConnectException;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;

@Configuration
public class FastApiRetryConfig {

    @Bean
    public Retry fastApiRetry(FastApiProperties fastApiProperties) {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(fastApiProperties.getRetryMaxAttempts())
                .waitDuration(fastApiProperties.getRetryWaitDuration())
                .retryOnException(this::isRetryable)
                .build();
        return Retry.of("fastApiRetry", config);
    }

    private boolean isRetryable(Throwable throwable) {
        Throwable cause = unwrap(throwable);

        if (cause instanceof BaseException baseException) {
            ErrorCode errorCode = baseException.getErrorCode();
            return errorCode == ErrorCode.AI_SERVER_TIMEOUT
                    || errorCode == ErrorCode.AI_SERVER_CONNECTION_FAILED
                    || errorCode == ErrorCode.AI_SERVER_INTERNAL_ERROR;
        }

        return cause instanceof TimeoutException
                || cause instanceof HttpTimeoutException
                || cause instanceof HttpConnectTimeoutException
                || cause instanceof ConnectException
                || cause instanceof ResourceAccessException
                || cause instanceof HttpServerErrorException;
    }

    private Throwable unwrap(Throwable throwable) {
        Throwable current = throwable;
        while (current instanceof CompletionException && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}
