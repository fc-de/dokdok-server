---
name: 테스트작성
description: dokdok 컨벤션(BE-rules §11)대로 테스트 코드를 작성하고 실행·검증한다. 대상(서비스 메서드/변경 diff/QA 버그/클래스)에 대해 유형 결정 → 케이스 도출(해피+더티 필수) → 작성 → gradlew 실행 → 통과 확인. "테스트 짜줘", "테스트 코드 추가", "이거 테스트해줘" 등에 사용.
---

테스트 코드를 작성하는 스킬. **해피케이스만 작성 금지 — 더티케이스(실패·예외·경계) 필수**(3단계 게이트).

## 1단계: 필수 Read (생략 불가)
- `docs/dev-rules/BE-rules.md` §11(테스트 규약) — 작성 기준
- 테스트 대상 클래스(서비스/리포지토리/엔티티)
- **같은 도메인의 기존 테스트 1개** — 픽스처·헬퍼·스타일 복제용
  - 서비스: `src/test/java/com/dokdok/meeting/service/MeetingServiceTest.java`
  - 리포지토리: `src/test/java/com/dokdok/meeting/repository/MeetingMemberRepositoryTest.java`

## 2단계: 테스트 유형 결정

| 대상 | 유형 | 기반 |
|------|------|------|
| 서비스 비즈니스 로직·검증·상태전이 | **Mockito 단위** | `@ExtendWith(MockitoExtension)`, `@InjectMocks`/`@Mock` |
| 리포지토리 쿼리·소프트삭제·페이징·영속성 | **`@DataJpaTest`** | `@ActiveProfiles("test")`(H2), `TestEntityManager` |
| 컨트롤러 직렬화·`@Valid` 검증 | MockMvc | (드물게만) |

대부분은 **서비스 Mockito 단위 테스트**가 기본이다.

## 3단계: 케이스 도출 (CRITICAL — 더티케이스 필수)

대상 메서드마다 **아래를 모두** 도출한다. **해피케이스만 있으면 미완성 → 작성 중단하고 보완.**

| 분류 | 내용 | 예 |
|------|------|-----|
| ✅ 해피 | 정상 입력 → 기대 결과 | 확정 대기 약속 거절 → REJECTED |
| 🔴 예외 | 메서드가 던지는 **도메인 예외마다 1개** | 확정된 약속 거절 → `INVALID_MEETING_STATUS_CHANGE` |
| 🟠 경계 | null / 0 / 24시간 / 정원 초과 / 중복 / 상태 전이 끝값 | 시작 24h 이내, 정원 마감, 이미 취소 |
| 🟡 권한 | 모임장/소속/약속장 등 인가 실패 | 비모임장이 거절 시도 |
| 🔁 회귀 | **버그 수정이면 그 버그 재현 케이스부터** | QA가 본 500/불일치 재현 |

게이트: **변경/대상 메서드별로 "해피 ≥1 + 더티(예외·경계·권한 중) ≥1"** 이 없으면 작성 완료로 보지 않는다. 던지는 예외가 여러 개면 **각각** 테스트한다.

## 4단계: 작성 규약 (dokdok 패턴)

### 공통
- 구조 `// given / // when / // then`, 메서드명 `given_when_then`, **`@DisplayName` 은 한글**로 의도 명시.

### 서비스 Mockito 단위
```java
@ExtendWith(MockitoExtension.class)
class XxxServiceTest {
    @InjectMocks XxxService service;
    @Mock XxxRepository repository;
    @Mock XxxValidator validator;

    @Test @DisplayName("확정된 약속은 거절할 수 없다.")
    void givenConfirmedMeeting_whenReject_thenThrow() {
        given(validator.findMeetingOrThrow(id)).willReturn(confirmed);          // BDDMockito
        try (MockedStatic<SecurityUtil> m = mockStatic(SecurityUtil.class)) {   // 현재 사용자 목
            m.when(SecurityUtil::getCurrentUserId).thenReturn(10L);
            // 🔴 예외는 errorCode 로 검증 (메시지 문자열 아님)
            assertThatThrownBy(() -> service.rejectMeeting(id))
                    .isInstanceOf(MeetingException.class)
                    .extracting("errorCode").isEqualTo(MeetingErrorCode.INVALID_MEETING_STATUS_CHANGE);
        }
    }
}
```

### 리포지토리 @DataJpaTest
```java
@DataJpaTest
@ActiveProfiles("test")
class XxxRepositoryTest {
    @Autowired XxxRepository repository;
    @Autowired TestEntityManager em;
    // persist 헬퍼 + em.flush()/em.clear() 후 조회 검증. 기존 테스트의 헬퍼 재사용.
}
```

- 예외 단언은 `assertThatThrownBy(...).extracting("errorCode").isEqualTo(...)` — **errorCode 기준**.
- 기존 테스트의 `setUp()`·픽스처·persist 헬퍼를 **재사용**(중복 정의 금지).

## 5단계: 실행·확인
```bash
./gradlew test --tests "com.dokdok.<도메인>.<...>Test" --console=plain
```
- **통과 확인 필수.** 실패하면 원인 분석 후 수정(테스트 또는 대상 코드) → 재실행. 결과를 사용자에게 보고.

## 6단계: 한계 인지 (보고에 명시)
- H2 `@DataJpaTest` 는 `create-drop` 이라 매번 최신 enum/스키마로 만든다 → **운영 스키마 의존 버그(enum CHECK 제약 등, BE-rules B10)는 재현 못 한다.** 그런 버그는 테스트로 안 잡히니 DB 조치가 별도임을 명시.
- 트랜잭션·지연로딩 관련 버그는 단위(목) 테스트로 안 잡힐 수 있음 → 필요 시 `@DataJpaTest`/통합으로.

## 주의
- **getter/builder/단순 매핑 등 자명한 코드 테스트 금지** — 의미 있는 분기·검증·쿼리만.
- 테스트를 통과시키려 대상 코드의 검증을 약화시키지 말 것(테스트가 스펙을 따라가야 함).
