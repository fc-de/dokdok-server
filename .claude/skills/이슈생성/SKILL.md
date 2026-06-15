---
name: 이슈생성
description: .github 이슈 템플릿 기반으로 fc-de/dokdok-server 에 GitHub 이슈를 생성한다. 유형 판별 → 제목/라벨 컨벤션 자동 적용 → 본문 작성 → 생성 전 사용자 확인(필수) → gh 로 생성. "이슈 만들어", "버그 등록", "기능 요청 이슈" 등에 사용.
---

GitHub 이슈를 생성하는 스킬. **사용자 확인 없이 자동 생성 금지** (4단계 게이트 필수).

대상 레포: **fc-de/dokdok-server** (origin). 다른 레포면 사용자가 명시한 레포로 대체.

## 0단계: 도구·대상 확인
```bash
gh auth status                      # 미인증이면 중단 → 사용자에게 `gh auth login` 안내
gh repo view fc-de/dokdok-server --json name -q .name
```

## 1단계: 필수 Read (생략 불가)
- `.github/ISSUE_TEMPLATE/feature_request.md` — 기능 요청 템플릿 (기능 이슈일 때 그대로 채움)
- 버그 이슈는 별도 템플릿 파일이 없으므로 **3단계의 "버그 양식"** 사용

## 2단계: 유형 판별 + 라벨 결정

| 유형 | 제목 접두 | type 라벨 |
|------|----------|-----------|
| 기능 | `[Feature]` | `type:feat` |
| 버그 | `[Bug]` | `type:fix` |
| 리팩토링 | `[Refactor]` | `type:refactor` |
| 문서 | `[Docs]` | `documentation` |
| 설정/환경 | `[Chore]` | `type:chore` |
| 테스트 | `[Test]` | `type:test` |

**도메인 라벨**(해당하면 1개 이상 추가): `domain:gathering` `domain:meeting` `domain:topic` `domain:keyword` `domain:topic_like` `domain:users` `domain:gathering_member` `domain:meeting_member` `domain:reading_summary` `domain:book` `domain:retrospective`
→ 이슈가 닿는 도메인 기준. 모호하면 **생략하고 사용자에게 1줄로 물어본다**(추측 라벨 금지).

## 3단계: 제목·본문 작성

**제목**: `[Bug] 거절 버튼 클릭 시 확정 대기 약속 거절 불가` 처럼 접두 + 핵심 요약. 60자 이내.

### 본문 — 기능 (feature_request 템플릿 채움)
```markdown
## 요청하는 기능(Feature Summary)
<한두 문장 요약>

## 상세 기능 설명(Details)
- [ ] <세부 1>
- [ ] <세부 2>

## 참고 UI/예상 화면(Optional)

## 관련 문서/링크(Optional)
- API: `<엔드포인트>`
```

### 본문 — 버그 (dokdok QA 글 양식)
```markdown
## 이슈 내용
<무엇이 잘못됐는지 한두 문장>

## 기대 결과
<정상 동작>

## 실제 결과
<실제로 일어난 일 / 에러 메시지·코드>

## 재현 방법
1. <단계>

## 발생 조건/환경
- 역할/상태: <예: 모임장, 확정 대기 약속>
- 환경: <dev / prod / 로컬>
```

## 4단계: 생성 전 확인 (HITL — 자동 생성 절대 금지)
제목 · 라벨 · 본문 **전체**를 사용자에게 보여주고 **"이대로 생성할까요?"** 명시 확인.
- 사용자가 수정 요청하면 반영 후 다시 확인.
- 승인 전에는 5단계 진입 금지.

## 5단계: 생성
본문은 줄바꿈/마크다운 안전을 위해 **임시 파일 → `--body-file`** 로 넘긴다.
```bash
cat > /tmp/issue-body.md <<'EOF'
<3단계 본문 그대로>
EOF

gh issue create \
  --repo fc-de/dokdok-server \
  --title "[Bug] <요약>" \
  --label "type:fix,domain:meeting" \
  --body-file /tmp/issue-body.md
```
생성 후 출력된 **이슈 URL/번호를 사용자에게 보고**. (이 번호는 이후 `PR생성` 스킬에서 연결에 사용)

## 주의
- 존재하지 않는 라벨을 `--label` 에 넣으면 실패한다. 2단계 표·도메인 목록의 라벨만 사용.
- 같은 내용의 기존 이슈가 있는지 `gh issue list --repo fc-de/dokdok-server --search "<키워드>"` 로 한 번 확인 후 생성하면 중복을 줄일 수 있다.
