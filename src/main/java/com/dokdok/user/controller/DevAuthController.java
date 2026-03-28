package com.dokdok.user.controller;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.oauth2.CustomOAuth2User;
import com.dokdok.user.entity.User;
import com.dokdok.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Profile({"dev", "local"})
@Tag(name = "[Dev] 인증", description = "개발/로컬 환경 전용 테스트 로그인 API (prod에서는 비활성화)")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class DevAuthController {

	private static final String DEV_PASSWORD = "dev1234";

	private final UserRepository userRepository;

	@Operation(
			summary = "테스트 로그인 (dev/local 전용)",
			description = "이메일과 고정 비밀번호(dev1234)로 세션을 생성합니다. dev/local 프로필에서만 사용 가능합니다."
	)
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "200",
					description = "로그인 성공",
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON_VALUE,
							examples = @ExampleObject(
									value = "{\"code\":\"SUCCESS\",\"message\":\"로그인 성공\",\"data\":null}"
							)
					)
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "400",
					description = "비밀번호 불일치 또는 존재하지 않는 이메일",
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON_VALUE,
							examples = {
									@ExampleObject(
											name = "비밀번호 불일치",
											value = "{\"code\":\"INVALID_PASSWORD\",\"message\":\"비밀번호가 일치하지 않습니다.\",\"data\":null}"
									),
									@ExampleObject(
											name = "이메일 없음",
											value = "{\"code\":\"USER_NOT_FOUND\",\"message\":\"존재하지 않는 이메일입니다.\",\"data\":null}"
									)
							}
					)
			)
	})
	@PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<Void>> devLogin(
			@RequestBody DevLoginRequest request,
			HttpServletRequest httpRequest) {

		if (!DEV_PASSWORD.equals(request.password())) {
			return ApiResponse.error("INVALID_PASSWORD", "비밀번호가 일치하지 않습니다.");
		}

		User user = userRepository.findByUserEmail(request.loginId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일: " + request.loginId()));

		CustomOAuth2User oAuth2User = CustomOAuth2User.builder()
				.user(user)
				.attributes(Map.of("id", user.getId()))
				.build();

		OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
				oAuth2User,
				oAuth2User.getAuthorities(),
				"kakao"
		);

		SecurityContextHolder.getContext().setAuthentication(authentication);
		httpRequest.getSession(true);

		return ApiResponse.success("로그인 성공");
	}

	public record DevLoginRequest(String loginId, String password) {}
}
