package com.daengddang.daengdong_map.common.exception;

import com.daengddang.daengdong_map.common.ErrorCode;
import lombok.Getter;

@Getter
public class AnalysisBackpressureException extends BaseException {

    private final int retryAfterSeconds;
    private final long activeTasks;
    private final int maxActiveTasks;

    public AnalysisBackpressureException(int retryAfterSeconds, long activeTasks, int maxActiveTasks) {
        super(ErrorCode.TOO_MANY_REQUESTS);
        this.retryAfterSeconds = retryAfterSeconds;
        this.activeTasks = activeTasks;
        this.maxActiveTasks = maxActiveTasks;
    }
}
