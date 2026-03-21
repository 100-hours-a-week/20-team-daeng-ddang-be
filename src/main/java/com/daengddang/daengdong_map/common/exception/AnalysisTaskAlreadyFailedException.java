package com.daengddang.daengdong_map.common.exception;

public class AnalysisTaskAlreadyFailedException extends RuntimeException {

    public AnalysisTaskAlreadyFailedException(String taskId) {
        super("분석 작업이 이미 실패 상태라 DLQ로 보내야 합니다. taskId=" + taskId);
    }
}
