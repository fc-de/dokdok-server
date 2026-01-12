package com.dokdok.oauth2.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OAuth2ErrorCode {

    // OAuth2 인증 관련 (O0xx)
    INVALID_OAUTH_PROVIDER("O001", "지원하지 않는 소셜 로그인입니다."),
    OAUTH_AUTHENTICATION_FAILED("O002", "OAuth 인증에 실패했습니다."),
    INVALID_USER_PRINCIPAL("O003", "사용자 인증 정보를 추출할 수 없습니다."),

    // 카카오 사용자 정보 추출 (O1xx)
    INVALID_KAKAO_ID("O101", "카카오 ID를 추출할 수 없습니다."),
    INVALID_KAKAO_EMAIL("O102", "카카오 이메일 정보가 올바르지 않습니다."),
    INVALID_KAKAO_RESPONSE("O103", "카카오 응답 데이터가 올바르지 않습니다.");

    private final String code;
    private final String message;
}
