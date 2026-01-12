package com.dokdok.oauth2.service;

import com.dokdok.oauth2.CustomOAuth2User;
import com.dokdok.oauth2.KakaoUserInfo;
import com.dokdok.oauth2.OAuth2UserInfo;
import com.dokdok.oauth2.exception.OAuth2ErrorCode;
import com.dokdok.oauth2.exception.OAuth2Exception;
import com.dokdok.user.entity.User;
import com.dokdok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        try {
            // OAuth2 Provider로부터 사용자 정보 가져온다.
            OAuth2User oAuth2User = super.loadUser(userRequest);

            // Provider 식별 (kakao, naver)
            String registrationId = userRequest.getClientRegistration().getRegistrationId();

            // Provider별 사용자 정보 추출
            OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

            // 사용자 정보 처리 (회원가입)
            User user = processOAuthUser(oAuth2UserInfo);

            // UserPrincipal 반환 (Security 에서 사용)
            return new CustomOAuth2User(user, oAuth2User.getAttributes());
        } catch (OAuth2Exception e) {

            // OAuth2Exception을 OAuth2AuthenticationException으로 변환
            log.error("OAuth2 처리 중 오류 발생: code={}, message={}",
                    e.getErrorCode().getCode(), e.getMessage());

            throw new OAuth2AuthenticationException(
                    new OAuth2Error(e.getErrorCode().getCode(), e.getErrorCode().getMessage(), null),
                    e.getMessage(),
                    e
            );
        }
    }

    private User processOAuthUser(OAuth2UserInfo oAuth2UserInfo) {

        return userRepository.findByKakaoId(oAuth2UserInfo.getId())
                .orElseGet(() -> {
                    // 신규 사용자 정보 저장
                    User newUser = User.of(oAuth2UserInfo);

                    log.info("신규 사용자 생성 : kakao_id = {}",  oAuth2UserInfo.getId());
                    return userRepository.save(newUser);
                });
    }

    /**
     * Provider별 사용자 정보 추출
     */
    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {

        if("kakao".equals(registrationId)) {
            return new KakaoUserInfo(attributes);
        }

        throw new OAuth2Exception(OAuth2ErrorCode.INVALID_OAUTH_PROVIDER,
                "지원하지 않는 소셜 로그인입니다: " + registrationId);
    }

}
