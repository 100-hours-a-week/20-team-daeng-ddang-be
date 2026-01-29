package com.daengddang.daengdong_map.common;

import lombok.Getter;

@Getter
public enum SuccessCode {

    LOGIN_SUCCESS("로그인에 성공했습니다."),
    KAKAO_LOGIN_URL_CREATED("카카오 로그인 URL을 생성했습니다."),
    TOKEN_REFRESHED("토큰이 갱신되었습니다."),
    LOGOUT_SUCCESS("로그아웃 되었습니다."),
    AUTHORIZATION_CODE_DELIVERED("인가 코드가 전달되었습니다."),

    USER_INFO_REGISTERED("사용자 정보 등록에 성공했습니다."),
    USER_INFO_UPDATED("사용자 정보 수정에 성공했습니다."),
    USER_INFO_RETRIEVED("사용자 정보 조회에 성공했습니다."),
    USER_DELETED("회원 탈퇴가 완료되었습니다."),

    DOG_REGISTERED("강아지 정보 등록에 성공했습니다."),
    DOG_INFO_RETRIEVED("강아지 정보 조회에 성공했습니다."),
    DOG_INFO_UPDATED("강아지 정보 수정에 성공했습니다."),
    DOG_BREED_LIST_RETRIEVED("강아지 종 목록 조회에 성공했습니다."),

    REGION_LIST_RETRIEVED("지역 목록 조회에 성공했습니다."),
    REGION_RETRIEVED("지역 조회에 성공했습니다."),

    WALK_STARTED("산책이 시작되었습니다."),
    WALK_ENDED("산책이 정상적으로 종료되었습니다."),
    OCCUPIED_BLOCKS_RETRIEVED("점유 블록 조회에 성공했습니다."),
    NEARBY_BLOCKS_RETRIEVED("주변 블록 조회에 성공했습니다."),

    MY_PAGE_SUMMARY_RETRIEVED("마이페이지 요약 조회에 성공했습니다."),
    USER_ACHIEVEMENT_LIST_RETRIEVED("사용자 업적 목록 조회에 성공했습니다."),
    RECAP_LIST_RETRIEVED("리캡 목록 조회에 성공했습니다."),
    RECAP_DETAIL_RETRIEVED("리캡 상세 조회에 성공했습니다."),

    PERSONAL_RANKING_SUMMARY_RETRIEVED("개인 랭킹 요약 조회에 성공했습니다."),
    PERSONAL_RANKING_LIST_RETRIEVED("개인 랭킹 목록 조회에 성공했습니다."),
    REGION_RANKING_SUMMARY_RETRIEVED("지역 랭킹 요약 조회에 성공했습니다."),
    REGION_RANKING_LIST_RETRIEVED("지역 랭킹 목록 조회에 성공했습니다."),
    REGION_CONTRIBUTION_RANKING_LIST_RETRIEVED("지역 기여도 랭킹 목록 조회에 성공했습니다."),

    PRESIGNED_URL_ISSUED("Presigned URL 발급에 성공했습니다."),

    EMOTION_ANALYSIS_RESULT_CREATED("표정 분석 결과 생성에 성공했습니다."),
    EMOTION_ANALYSIS_RESULT_RETRIEVED("표정 분석 결과 조회에 성공했습니다."),
    WALK_DIARY_RETRIEVED("산책일지 조회에 성공했습니다."),
    WALK_DIARY_CREATED("산책일지가 작성되었습니다."),
    WALK_IMAGE_LIST_RETRIEVED("산책 이미지 목록 조회에 성공했습니다."),

    CALENDAR_RECORD_LIST_RETRIEVED("캘린더 기록 조회에 성공했습니다."),
    DAILY_RECORD_LIST_RETRIEVED("일별 기록 목록 조회에 성공했습니다."),

    HEALTHCARE_ANALYSIS_RESULT_RETRIEVED("헬스케어 분석 결과 조회에 성공했습니다."),

    CONSULT_SESSION_CREATED("상담 세션이 생성되었습니다."),
    CONSULT_RESPONSE_CREATED("상담 답변이 생성되었습니다."),

    MISSION_RECORD_SAVED("돌발미션 기록이 저장되었습니다."),
    MISSION_ANALYSIS_COMPLETED("돌발미션 분석이 완료되었습니다."),
    MISSION_ANALYSIS_RESULT_RETRIEVED("돌발미션 분석 결과 조회에 성공했습니다."),
    MISSION_LIST_RETRIEVED("미션 목록 조회에 성공했습니다."),
    MISSION_UPLOAD_SAVED("돌발미션 업로드 정보가 저장되었습니다."),
    MISSION_UPLOAD_LIST_RETRIEVED("돌발미션 업로드 목록 조회에 성공했습니다."),

    HEALTH_CHECK_OK("success");

    private final String message;

    SuccessCode(String message) {
        this.message = message;
    }
}
