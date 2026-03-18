package com.daengddang.daengdong_map.common.exception;

public class AnalysisTaskAlreadySucceededException extends RuntimeException {

    public AnalysisTaskAlreadySucceededException(String taskId) {
        super("분석 작업이 이미 성공 상태라 중복 메시지를 건너뜁니다. taskId=" + taskId);
    }
}
