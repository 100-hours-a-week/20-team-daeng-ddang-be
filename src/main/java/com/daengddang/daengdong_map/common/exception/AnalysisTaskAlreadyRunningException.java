package com.daengddang.daengdong_map.common.exception;

public class AnalysisTaskAlreadyRunningException extends RuntimeException {

    public AnalysisTaskAlreadyRunningException(String taskId) {
        super("분석 작업이 이미 처리 중이라 중복 실행을 건너뜁니다. taskId=" + taskId);
    }
}
