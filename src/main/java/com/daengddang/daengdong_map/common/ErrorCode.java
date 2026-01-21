package com.daengddang.daengdong_map.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    /* =======================
     * 5.1 공통 / 시스템 오류
     * ======================= */
    PAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 페이지입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 리소스입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "올바른 메서드 접근이 아닙니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류"),
    SERVICE_UNDER_MAINTENANCE(HttpStatus.SERVICE_UNAVAILABLE, "서버 점검중입니다."),

    /* =======================
     * 5.2 인증 / 인가
     * ======================= */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "권한이 없는 사용자입니다. 로그인 후 이용해주세요."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    /* =======================
     * 5.3 OAuth / 토큰
     * ======================= */
    AUTHORIZATION_CODE_INVALID_FORMAT(HttpStatus.BAD_REQUEST,
            "인증 코드 형식이 올바르지 않거나 존재하지 않습니다."),
    AUTHORIZATION_CODE_EXPIRED(HttpStatus.UNAUTHORIZED,
            "인증 코드가 만료됐습니다. 다시 로그인 해주세요."),
    INVALID_AUTHORIZATION_CODE(HttpStatus.UNAUTHORIZED,
            "유효하지 않은 인증 코드입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,
            "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED,
            "만료된 리프레시 토큰입니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED,
            "리프레시 토큰이 일치하지 않습니다."),
    LOGIN_SERVER_COMMUNICATION_FAILED(HttpStatus.BAD_GATEWAY,
            "로그인 서버와 통신에 실패했습니다."),

    /* =======================
     * 5.4 요청 형식 / 검증
     * ======================= */
    INVALID_FORMAT(HttpStatus.BAD_REQUEST, "유효하지 않은 형식입니다."),
    INVALID_PERIOD_FORMAT(HttpStatus.BAD_REQUEST, "유효하지 않은 기간 형식입니다."),
    INVALID_DATE_REQUEST(HttpStatus.BAD_REQUEST, "유효하지 않은 날짜 요청입니다."),
    SEARCH_KEYWORD_TOO_SHORT(HttpStatus.BAD_REQUEST, "검색어는 1자 이상 입력해주세요."),
    INVALID_REGION_FILTER(HttpStatus.BAD_REQUEST, "유효하지 않은 지역 조회 조건입니다."),

    /* =======================
     * 5.5 사용자 / 강아지
     * ======================= */
    NAME_REQUIRED(HttpStatus.BAD_REQUEST, "이름을 입력해주세요."),
    NAME_RULE_VIOLATION(HttpStatus.BAD_REQUEST,
            "이름은 2자 이상 15자 이하이며 한글 또는 영어만 가능합니다."),
    DOG_BREED_REQUIRED(HttpStatus.BAD_REQUEST, "강아지 종을 선택해주세요."),
    DOG_BREED_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 강아지 종입니다."),
    DOG_BIRTHDATE_REQUIRED(HttpStatus.BAD_REQUEST, "강아지 생년월일을 입력해주세요."),
    DOG_WEIGHT_REQUIRED(HttpStatus.BAD_REQUEST, "강아지 몸무게를 입력해주세요."),
    DOG_WEIGHT_DECIMAL_LIMIT(HttpStatus.BAD_REQUEST,
            "강아지 몸무게는 소수점 첫째자리까지만 입력 가능합니다."),
    DOG_BREED_NAME_DUPLICATED(HttpStatus.CONFLICT, "이미 존재하는 강아지 종입니다."),

    /* =======================
     * 5.6 지역 / 조회
     * ======================= */
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 지역입니다."),
    WALK_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 산책 기록입니다."),
    ANALYSIS_TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 분석 작업입니다."),

    /* =======================
     * 5.7 파일 / 미디어
     * ======================= */
    INVALID_FILE_URL(HttpStatus.BAD_REQUEST, "파일 URL이 올바르지 않습니다."),
    VIDEO_LENGTH_EXCEEDED(HttpStatus.BAD_REQUEST, "영상 길이가 제한을 초과했습니다."),
    FILE_TYPE_UNSUPPORTED(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "지원하지 않는 파일 타입입니다."),

    /* =======================
     * 5.8 AI / 분석
     * ======================= */
    EMOTION_ANALYSIS_ALREADY_RUNNING(HttpStatus.CONFLICT,
            "이미 분석이 진행 중이거나 완료된 산책입니다."),
    MISSION_ALREADY_ANALYZED(HttpStatus.CONFLICT,
            "이미 돌발 미션 분석이 완료된 영상입니다."),
    DOG_FACE_NOT_RECOGNIZED(HttpStatus.UNPROCESSABLE_ENTITY,
            "영상에서 강아지 얼굴을 인식할 수 없습니다."),
    CHAT_ANALYSIS_NOT_SUPPORTED(HttpStatus.UNPROCESSABLE_ENTITY,
            "해당 채팅 이미지 및 텍스트를 분석할 수 없습니다."),
    AI_SERVER_CONNECTION_FAILED(HttpStatus.BAD_GATEWAY,
            "AI 서버와 연결에 실패했습니다."),

    /* =======================
     * 5.9 산책 / 세션
     * ======================= */
    INVALID_WALK_METRICS(HttpStatus.BAD_REQUEST,
            "산책 거리 또는 시간이 올바르지 않습니다."),
    WALK_ALREADY_IN_PROGRESS(HttpStatus.CONFLICT, "이미 진행 중인 산책이 있습니다."),
    WALK_ALREADY_ENDED(HttpStatus.CONFLICT, "이미 종료된 산책입니다."),
    SESSION_EXPIRED(HttpStatus.CONFLICT, "종료된 세션입니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public String toResponseJson() {
        return """
        {
          "message": "%s",
          "data": null,
          "errorCode": "%s"
        }
        """.formatted(message, name());
    }

}
