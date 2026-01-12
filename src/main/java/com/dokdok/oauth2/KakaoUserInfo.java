package com.dokdok.oauth2;

import com.dokdok.oauth2.exception.OAuth2ErrorCode;
import com.dokdok.oauth2.exception.OAuth2Exception;

import java.util.Map;

public class KakaoUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public Long getId() {
        Object id = attributes.get("id");

        if (id == null) {
            throw new OAuth2Exception(OAuth2ErrorCode.INVALID_KAKAO_ID);
        }

        try {
            if (id instanceof Number) {
                return ((Number) id).longValue();
            }
            return Long.valueOf(String.valueOf(id));
        } catch (NumberFormatException e) {
            throw new OAuth2Exception(OAuth2ErrorCode.INVALID_KAKAO_ID, e);
        }
    }

    @Override
    public String getEmail() {
        try {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount == null) {
                return null;
            }
            return (String) kakaoAccount.get("email");
        } catch (ClassCastException e) {
            throw new OAuth2Exception(OAuth2ErrorCode.INVALID_KAKAO_EMAIL, e);
        }
    }
}
