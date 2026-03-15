package com.daengddang.daengdong_map.ai;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class FastApiCallExecutor {

    private final Retry fastApiRetry;
    private final Bulkhead fastApiBulkhead;
    private final CircuitBreaker fastApiCircuitBreaker;
    private final ExecutorService fastApiExecutorService;

    public <T> T execute(TimeLimiter timeLimiter, Supplier<T> supplier) throws Exception {
        Supplier<T> baseSupplier = () -> executeOnce(timeLimiter, supplier);
        Supplier<T> retrySupplier = Retry.decorateSupplier(fastApiRetry, baseSupplier);
        Supplier<T> circuitBreakerSupplier =
                CircuitBreaker.decorateSupplier(fastApiCircuitBreaker, retrySupplier);
        Supplier<T> bulkheadSupplier =
                Bulkhead.decorateSupplier(fastApiBulkhead, circuitBreakerSupplier);

        try {
            return bulkheadSupplier.get();
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof Exception exception) {
                throw exception;
            }
            throw ex;
        } catch (CallNotPermittedException ex) {
            throw new BaseException(ErrorCode.AI_SERVER_CIRCUIT_OPEN, ex);
        } catch (BulkheadFullException | RejectedExecutionException ex) {
            throw new BaseException(ErrorCode.AI_SERVER_BULKHEAD_REJECTED, ex);
        }
    }

    private <T> T executeOnce(TimeLimiter timeLimiter, Supplier<T> supplier) {
        Future<T> future;
        try {
            future = fastApiExecutorService.submit(supplier::get);
        } catch (RejectedExecutionException ex) {
            throw new CompletionException(new BaseException(ErrorCode.AI_SERVER_BULKHEAD_REJECTED, ex));
        }
        try {
            return timeLimiter.executeFutureSupplier(() -> future);
        } catch (Exception ex) {
            future.cancel(true);
            throw new CompletionException(ex);
        }
    }
}
