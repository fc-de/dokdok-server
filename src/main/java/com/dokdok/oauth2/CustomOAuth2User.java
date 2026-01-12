package com.dokdok.oauth2;

import com.dokdok.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    @NonNull
    private final User user;

    @NonNull
    private final Map<String, Object> attributes;

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getName() {
        return String.valueOf(user.getId());
    }

    // 편의 메서드
    public Long getUserId() {
        return user.getId();
    }

    public String getNickname() {
        return user.getNickname();
    }

}
