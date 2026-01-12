package com.dokdok.oauth2.exception;

import lombok.Getter;

@Getter
public class OAuth2Exception extends RuntimeException {

    private final OAuth2ErrorCode errorCode;

    public OAuth2Exception(OAuth2ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public OAuth2Exception(OAuth2ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public OAuth2Exception(OAuth2ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}