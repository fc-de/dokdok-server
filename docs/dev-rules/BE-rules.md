# 백엔드 개발 규칙 (BE Rules)

dokdok-server(Spring Boot 4 / Java 21 / PostgreSQL) 백엔드 개발 시 **반드시 준수**할 규칙.
Claude 가 코드를 작성·수정할 때 이 문서를 기준으로 한다. PR 생성 전 자가 점검의 기준이기도 하다.

> 적용 범위: `src/main/java/com/dokdok/**`. 예시는 실제 코드(`meeting` 도메인 등) 기준.

---

## 1. 패키지·레이어 구조 (B1)

도메인별로 아래 레이어를 둔다. `com.dokdok.<도메인>.{api, controller, dto, entity, exception, repository, service, scheduler}`

```
✅ com.dokdok.meeting.controller.MeetingController
✅ com.dokdok.meeting.service.MeetingService
✅ com.dokdok.meeting.service.MeetingValidator
❌ com.dokdok.controller.MeetingController   // 도메인 패키지 밖 금지
```

- 공통/전역 코드는 `com.dokdok.global.{response, exception, util, ...}`.
- 레이어 호출 방향: `Controller → Service → Repository`. Controller 가 Repository 직접 호출 금지.

---

## 2. 컨트롤러 + API 문서 분리 (B2) — CRITICAL

컨트롤러는 **Swagger 문서를 담은 인터페이스(`XxxApi`)를 구현**한다. 문서 애너테이션은 인터페이스에, 매핑·구현은 컨트롤러에 둔다.

```java
// api/MeetingApi.java — @Tag/@Operation/@ApiResponses 문서 전용
@Tag(name = "약속 관리", description = "약속 관련 API")
public interface MeetingApi {
    @Operation(summary = "약속 상세 조회 (developer: 김윤영)", description = "...")
    ResponseEntity<ApiResponse<MeetingDetailResponse>> findMeeting(Long meetingId);
}

// controller/MeetingController.java — 매핑·구현
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings")          // ✅ 반드시 /api/ 로 시작
public class MeetingController implements MeetingApi {
    private final MeetingService meetingService;

    @Override
    @GetMapping("/{meetingId}")
    public ResponseEntity<ApiResponse<MeetingDetailResponse>> findMeeting(@PathVariable Long meetingId) {
        return ApiResponse.success(meetingService.findMeeting(meetingId), "약속 상세 조회에 성공했습니다.");
    }
}
```

- `@Operation(summary)` 에 담당자 표기: `(developer: 이름)`.
- 경로는 **항상 `/api/` 로 시작**, 복수형 리소스명(`/api/meetings`).
- 컨트롤러는 **얇게** — 비즈니스 로직 금지, 서비스 호출 + `ApiResponse` 래핑만.

**위반 시**: Swagger 문서 누락, 라우팅/문서 불일치.

---

## 3. 응답 규약 (B3)

모든 응답은 `ApiResponse<T>`(record) 정적 팩토리로 감싼다. 직접 `new ResponseEntity` 금지.

| 동작 | 메서드 | 상태 | code |
|------|--------|------|------|
| 조회 | `ApiResponse.success(data, "메시지")` | 200 | SUCCESS |
| 생성 | `ApiResponse.created(data, "메시지")` | 201 | CREATED |
| 수정 | `ApiResponse.updated(data, "메시지")` | 200 | UPDATED |
| 삭제 | `ApiResponse.deleted("메시지")` | 200 | DELETED |

```java
✅ return ApiResponse.created(meetingService.createMeeting(request), "약속 생성 요청에 성공했습니다.");
❌ return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "...", data));   // 팩토리 우회 금지
```

---

## 4. 예외 처리 2계층 (B4) — CRITICAL

### 4-1. 도메인별 예외 + 에러코드 enum

각 도메인은 `XxxException extends BaseException` + `XxxErrorCode implements BaseErrorCode` 를 둔다.

```java
public enum MeetingErrorCode implements BaseErrorCode {
    MEETING_NOT_FOUND("M001", "약속을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_MEETING_STATUS_CHANGE("M009", "약속 상태를 변경할 수 없습니다.", HttpStatus.BAD_REQUEST);
    // code: 도메인 약자 1자 + 3자리 번호 (M001, B001 ...). message·status 필수.
}

throw new MeetingException(MeetingErrorCode.MEETING_NOT_FOUND);
throw new MeetingException(MeetingErrorCode.INVALID_MEETING_STATUS_CHANGE, "신청된 약속만 거절할 수 있습니다."); // 메시지 오버라이드 가능
```

### 4-2. raw 예외 금지 (E000 방지)

`BaseException` 외의 예외(NPE, `IllegalArgumentException`, `IllegalStateException`, DB 예외 등)가 컨트롤러 밖으로 새면 `GlobalExceptionHandler` 가 **E000 / 500** 으로 응답한다(`runtimeExceptionHandler`).

```java
✅ throw new BookException(BookErrorCode.BOOK_NOT_FOUND);
❌ throw new IllegalArgumentException("책 없음");   // → 500 E000, 클라이언트가 원인 모름
❌ Optional.get() / 무방비 .orElseThrow() 없이 null 접근
```

**위반 시**: 사용자에게 `{"code":"E000","message":"서버 에러..."}` 노출, 원인 추적 어려움.

---

## 5. 서비스 규약 (B5)

```java
@Service
@RequiredArgsConstructor                 // ✅ final 필드 생성자 주입
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final MeetingValidator meetingValidator;

    @Transactional(readOnly = true)      // ✅ 조회는 readOnly
    public MeetingDetailResponse findMeeting(Long meetingId) {
        Long userId = SecurityUtil.getCurrentUserId();   // ✅ 인증 사용자 ID
        ...
    }

    @Transactional                        // ✅ 변경은 readOnly 없이
    public MeetingStatusResponse rejectMeeting(Long meetingId) { ... }
}
```

- 주입은 **생성자 주입(`@RequiredArgsConstructor` + `final`)**. `@Autowired` 필드 주입 금지.
- 상태 변경 메서드 `@Transactional`, 순수 조회 `@Transactional(readOnly = true)`.
- 현재 사용자: `SecurityUtil.getCurrentUserId()` (인증 없으면 `GlobalException` 던짐).
- 응답 DTO 매핑은 **트랜잭션 안에서** 끝낸다(지연 로딩 `LazyInitializationException` 방지).

---

## 6. 검증은 Validator 컴포넌트로 분리 (B6)

존재·권한·소속·정원 등 횡단 검증은 `XxxValidator`(`@Component`)로 분리한다.

```java
meetingValidator.findMeetingOrThrow(meetingId);            // 존재 검증 + 반환
gatheringValidator.validateLeader(gatheringId, userId);    // 모임장 권한
gatheringValidator.validateMembership(gatheringId, userId);// 모임 소속
meetingValidator.validateCapacity(meetingId, max);         // 정원
```

- 비즈니스 규칙 검증(상태 전이 가능 여부 등)은 서비스 private 메서드 또는 엔티티 메서드에서 **명시적 도메인 예외**로.
- "조회 후 null 체크" 대신 `findXxxOrThrow` 패턴 사용.

---

## 7. 엔티티 규약 (B7)

```java
@Entity
@Table(name = "meeting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // ✅ 기본 생성자 PROTECTED
@SuperBuilder
@SQLDelete(sql = "UPDATE meeting SET deleted_at = CURRENT_TIMESTAMP WHERE meeting_id = ?")  // soft delete
@SQLRestriction("deleted_at IS NULL")
public class Meeting extends BaseTimeEntity {        // ✅ 공통 시간/소프트삭제 상속

    @Enumerated(EnumType.STRING)                     // ✅ enum 은 STRING (10번 규칙 주의)
    private MeetingStatus meetingStatus = MeetingStatus.PENDING;

    public static Meeting create(MeetingCreateRequest req, ...) { ... }  // ✅ 정적 팩토리

    public void changeStatus(MeetingStatus target) {  // ✅ 상태 전이·검증을 엔티티가 책임
        if (this.meetingStatus == MeetingStatus.DONE) {
            throw new MeetingException(MeetingErrorCode.INVALID_MEETING_STATUS_CHANGE, "...");
        }
        this.meetingStatus = target;
    }
}
```

- **Setter 금지**. 생성은 정적 `create(...)`, 변경은 의미 있는 도메인 메서드(`changeStatus`, `update`)로.
- 모든 엔티티는 `BaseTimeEntity` 상속(createdAt/updatedAt/deletedAt + 감사).
- 삭제는 **soft delete**(`@SQLDelete` + `@SQLRestriction`), 물리 삭제 지양.
- 연관관계는 `FetchType.LAZY` 기본.

---

## 8. DTO 규약 (B8)

```java
@Schema(description = "약속 응답")
public record MeetingResponse(                       // ✅ record
        @Schema(description = "약속 ID", example = "1") Long meetingId,
        @Schema(description = "약속 상태", example = "CONFIRMED") MeetingStatus meetingStatus
) {
    public static MeetingResponse from(Meeting meeting, List<MeetingMember> members) { ... } // ✅ 정적 from()
}

@Builder
public record MeetingCreateRequest(
        @NotNull Long gatheringId,                    // ✅ jakarta validation
        @Valid @NotNull BookInfo book,
        @Size(min = 1, max = 24) String meetingName
) {}
```

- 요청 `XxxRequest`, 응답 `XxxResponse`, 모두 **record**.
- 엔티티 → DTO 변환은 DTO 의 **정적 `from(...)`** 팩토리. 엔티티를 컨트롤러/응답에 **직접 노출 금지**.
- 요청 검증은 `jakarta.validation`(`@NotNull`, `@NotBlank`, `@Size`, 중첩은 `@Valid`) + 컨트롤러 `@Valid`.
- 모든 필드에 `@Schema`(설명/example) 부여.

---

## 9. Repository 규약 (B9)

```java
public interface MeetingMemberRepository extends JpaRepository<MeetingMember, Long> {

    @Query("""
            SELECT count(mm) FROM MeetingMember mm
            JOIN mm.meeting m
            WHERE mm.user.id = :userId
            AND mm.canceledAt IS NULL                 -- ✅ soft delete / 취소 인지
            AND m.meetingStatus IN :statuses
            """)
    int countMeetingsByUserIdAndGatheringIdAndStatusIn(
            @Param("userId") Long userId,
            @Param("statuses") List<MeetingStatus> statuses);
}
```

- JPQL 은 텍스트 블록 + `@Param`. 파라미터 바인딩만(문자열 연결 금지).
- 취소/삭제 인지 조건(`canceledAt IS NULL` 등)을 빠뜨리지 말 것.
- 페이지네이션: `Page`/`Pageable`, 커서 기반은 `CursorResponse`.
- **카운트 쿼리와 목록 쿼리의 조건을 일치**시킬 것 — 탭 뱃지 수와 실제 목록 수가 어긋나는 버그의 원인(과거 JOINED 탭 사례).

---

## 10. enum ↔ DB CHECK 제약 (B10) — CRITICAL

`@Enumerated(STRING)` 컬럼은 Hibernate 가 `CHECK (col IN (...))` 제약을 **테이블 생성 시점의 enum 값으로** 만든다. prod 는 `ddl-auto: none`, dev/기본은 `update` 인데 **둘 다 기존 CHECK 제약을 갱신하지 않는다.**

→ **이미 운영 중인 enum 컬럼에 새 값을 추가**하면, 그 값으로 UPDATE/INSERT 시 제약 위반 → `DataIntegrityViolationException` → **500(E000)**.

```sql
-- enum 값 추가 PR 이면 운영 DB 제약 갱신 SQL 을 src/main/resources/data/ 에 함께 넣는다.
ALTER TABLE meeting DROP CONSTRAINT IF EXISTS meeting_meeting_status_check;
ALTER TABLE meeting ADD CONSTRAINT meeting_meeting_status_check
    CHECK (meeting_status IN ('PENDING','CONFIRMED','REJECTED','DONE'));
```

- H2 `@DataJpaTest`(create-drop)는 매번 최신 enum 으로 스키마를 만들어 **이 버그를 재현 못 한다** → 코드 테스트만 믿지 말 것.
- 실제 사례: `MeetingStatus.REJECTED` 추가 후 운영 제약 미갱신으로 거절 시 500 발생.
- 스키마 변경 SQL 은 `src/main/resources/data/*.sql` 에 기록(수동 적용 관례).

---

## 11. 테스트 규약 (B11)

### 11-1. 서비스 = Mockito 단위 테스트
```java
@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {
    @InjectMocks MeetingService meetingService;
    @Mock MeetingRepository meetingRepository;

    @Test
    @DisplayName("확정 대기 약속은 정상적으로 거절된다.")     // ✅ 한글 DisplayName
    void givenPendingMeeting_whenReject_thenRejected() {
        // given / when / then 구조
        given(meetingValidator.findMeetingOrThrow(id)).willReturn(pending);   // BDDMockito
        try (MockedStatic<SecurityUtil> m = mockStatic(SecurityUtil.class)) { // 정적 메서드 목
            m.when(SecurityUtil::getCurrentUserId).thenReturn(10L);
            ...
        }
    }
}
```

### 11-2. 리포지토리/영속성 = @DataJpaTest (H2)
```java
@DataJpaTest
@ActiveProfiles("test")          // H2 in-memory, create-drop
class MeetingMemberRepositoryTest {
    @Autowired MeetingRepository meetingRepository;
    @Autowired TestEntityManager em;
}
```

- 메서드명 `given_when_then`, `@DisplayName` 은 한글로 의도 설명.
- **해피케이스만 작성 금지 — 더티케이스 필수.** 대상 메서드마다 정상(해피) + 더티(예외·경계·권한 실패) 케이스를 함께 둔다. 메서드가 던지는 **도메인 예외는 각각** 테스트(`assertThatThrownBy ... extracting("errorCode")`).
- **QA 버그를 고치면 회귀 테스트를 같이 추가**(재현 → 수정 → 통과).
- 단, 10번(CHECK 제약)처럼 운영 스키마 의존 버그는 H2 로 재현 안 됨을 인지.
- 자동화: `/테스트작성` 스킬이 이 규약을 강제한다.

---

## 12. 커밋 · 브랜치 · PR (B12)

- 커밋 메시지: `type: 한글 설명` (type = feat/fix/refactor/chore/test/docs). 예) `fix: 약속 거절 시 500 오류 수정`.
- 작업은 **feature 브랜치**(`<type>/<요약>`)에서. `dev` 직접 커밋 금지.
- 이슈/PR 생성은 `/이슈생성`, `/PR생성` 스킬 사용(제목·라벨 컨벤션 자동 적용).
- base 브랜치는 `dev`.

---

## 13. 보안 · 설정 (B13)

- 인증 사용자 조회는 `SecurityUtil`. 인가(모임장/소속) 검증은 Validator 로 명시적으로.
- DB 계정·토큰·키 등 시크릿은 **환경변수**(`${...}`)로만. yaml/코드 하드코딩 금지.
- prod 설정(`application-prod.yaml`)은 `ddl-auto: none` — 스키마 변경은 SQL 로 수동 반영(10번 참고).

---

## 14. 산출물 동기화 (B14) — CRITICAL

코드만 바꾸고 끝내지 않는다. 아래 변경은 **대응 산출물을 같은 PR 에서 함께 갱신**한다.

### 14-1. 에러코드 추가/변경 → `docs/ErrorCode.md` 갱신

`XxxErrorCode` enum 에 코드를 추가/수정/삭제하면 **`docs/ErrorCode.md`** 의 해당 도메인 표에 행을 함께 반영한다.

```java
// MeetingErrorCode.java 에 추가
MEETING_REJECT_NOT_ALLOWED("M025", "거절할 수 없는 약속입니다.", HttpStatus.BAD_REQUEST),
```
```markdown
<!-- docs/ErrorCode.md 의 Meeting 표에 같은 행 추가 -->
| M025 | MEETING_REJECT_NOT_ALLOWED | 거절할 수 없는 약속입니다. | 400 |
```

- 코드 체계 유지: 도메인 약자 1자 + 3자리 번호(`M0xx`, `B0xx` ...). 기존 번호 재사용·중복 금지.
- 메시지·HTTP 상태는 enum 과 문서가 **일치**해야 한다.

**위반 시**: 에러코드 문서가 실제 코드와 어긋나 FE/QA 가 잘못된 명세를 본다.

### 14-2. API 추가/변경 → 도메인 `api/XxxApi` Swagger 인터페이스 갱신

엔드포인트 추가·시그니처 변경·요청/응답 DTO 변경 시, 해당 도메인의 **`<도메인>/api/XxxApi`** 인터페이스(Swagger 문서)도 함께 고친다. (B2 — 문서는 컨트롤러가 아니라 인터페이스에 모여 있음)

```
✅ POST /api/meetings/{id}/reject 추가
   → MeetingController 에 매핑 + MeetingApi 인터페이스에 @Operation/@ApiResponses/example 추가
❌ 컨트롤러에만 추가하고 MeetingApi 미갱신   // 문서 누락
```

갱신 대상(해당 시):
- `@Operation`(summary/description, `(developer: 이름)`), `@Parameter`
- `@ApiResponses` 의 성공/실패 예시(`@ExampleObject`) — 실제 응답 형태와 일치
- 요청/응답 record 변경이면 `@Schema(implementation = ...)` 도 함께

**위반 시**: Swagger 문서와 실제 API 불일치 → FE 연동 오류.

---

## 자가 점검 요약 (PR 전)

| ID | 항목 | 확인 |
|----|------|------|
| B2 | 컨트롤러가 `XxxApi` 구현, `/api/` 경로, 로직 없음 | |
| B3 | 응답을 `ApiResponse` 팩토리로 래핑 | |
| B4 | 도메인 예외만 던짐(raw 예외/E000 없음) | |
| B5 | 생성자 주입 + `@Transactional(readOnly)` 적절 | |
| B7 | 엔티티 setter 없음, 도메인 메서드로 변경, soft delete | |
| B8 | DTO record + `from()` + 엔티티 직접 노출 없음 + 검증 | |
| B9 | 쿼리 soft-delete 조건, 카운트=목록 조건 일치 | |
| B10 | enum 값 추가 시 DB CHECK 제약 갱신 SQL 포함 | |
| B11 | 회귀 테스트 추가, DisplayName 한글 | |
| B14-1 | 에러코드 추가/변경 시 `docs/ErrorCode.md` 동기화 | |
| B14-2 | API 추가/변경 시 도메인 `api/XxxApi` Swagger 인터페이스 갱신 | |
