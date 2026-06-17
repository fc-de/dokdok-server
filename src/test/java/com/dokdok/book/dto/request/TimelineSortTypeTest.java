package com.dokdok.book.dto.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * QA: 책 상세 "감상 기록 란"이 안 뜨던 버그 회귀 테스트.
 * 원인 = FE 가 보내는 sort=LATEST 가 enum 상수에 없어 @RequestParam 바인딩
 * (Enum.valueOf) 이 실패 → MethodArgumentTypeMismatchException → G003.
 * 따라서 LATEST/OLDEST 가 유효한 상수여야 하고, 정렬 방향 매핑이 맞아야 한다.
 */
class TimelineSortTypeTest {

    @DisplayName("해피: FE 가 보내는 LATEST/OLDEST 가 enum 상수로 바인딩된다 (과거 G003 원인)")
    @ParameterizedTest
    @ValueSource(strings = {"LATEST", "OLDEST", "DESC", "ASC"})
    void valueOf_supportsFrontendAndLegacyTokens(String token) {
        assertThat(TimelineSortType.valueOf(token)).isNotNull();
    }

    @DisplayName("해피: 정렬 방향 매핑 - OLDEST/ASC 만 오름차순, LATEST/DESC 는 내림차순")
    @ParameterizedTest
    @CsvSource({
            "LATEST,false",
            "DESC,false",
            "OLDEST,true",
            "ASC,true"
    })
    void isAscending_mapsByMeaning(TimelineSortType sort, boolean ascending) {
        assertThat(sort.isAscending()).isEqualTo(ascending);
    }

    @DisplayName("더티: 정의되지 않은 토큰은 거부된다 (바인딩 단계에서 G003 으로 처리됨)")
    @Test
    void valueOf_rejectsUnknownToken() {
        assertThatThrownBy(() -> TimelineSortType.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
