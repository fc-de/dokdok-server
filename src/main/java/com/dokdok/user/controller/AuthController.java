package com.dokdok.user.controller;

import com.dokdok.global.exception.GlobalErrorCode;
import com.dokdok.global.exception.GlobalException;
import com.dokdok.global.response.ApiResponse;
import com.dokdok.oauth2.CustomOAuth2User;
import com.dokdok.user.dto.UserInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * 현재 로그인한 사용자의 세션 확인용
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User
    ) {
        if (oAuth2User == null) {
            throw new GlobalException(GlobalErrorCode.UNAUTHORIZED);
        }

        log.info("인증된 사용자 정보 조회: userId={}, nickname={}",
                oAuth2User.getUserId(), oAuth2User.getNickname());

        UserInfoResponse userInfo = UserInfoResponse.from(oAuth2User.getUser());

        return ApiResponse.success(userInfo, "로그인 사용자 정보 조회 성공");
    }

}
