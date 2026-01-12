package com.dokdok.oauth2.resolver;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                "/oauth2/authorization"
        );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);

        if (authorizationRequest != null) {
            // fe_origin 파라미터를 세션에 저장
            saveFrontendOrigin(request);
        }

        return authorizationRequest;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest =
                defaultResolver.resolve(request, clientRegistrationId);

        if (authorizationRequest != null) {
            // fe_origin 파라미터를 세션에 저장
            saveFrontendOrigin(request);
        }

        return authorizationRequest;
    }

    private void saveFrontendOrigin(HttpServletRequest request) {
        String feOrigin = request.getParameter("fe_origin");

        if (feOrigin != null && !feOrigin.isEmpty()) {
            request.getSession().setAttribute("fe_origin", feOrigin);
            log.info("FE Origin 저장: {}", feOrigin);
        }
    }
}