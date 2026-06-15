---
name: PR생성
description: .github PR 템플릿 기반으로 현재 브랜치에서 dev 대상 GitHub PR을 생성한다. 브랜치/커밋 점검 → 푸시 → 제목 컨벤션([#이슈][FIX]) + 라벨 적용 → PR 본문 작성 → 생성 전 사용자 확인(필수) → gh 로 생성. "PR 만들어", "풀리퀘 올려" 등에 사용.
---

현재 브랜치 변경을 **dev 대상 PR**로 생성하는 스킬. **사용자 확인 없이 자동 생성 금지** (6단계 게이트 필수).

대상 레포: **fc-de/dokdok-server** (origin), base 브랜치: **dev**.

## 0단계: 도구·대상 확인
```bash
gh auth status                                   # 미인증이면 중단
git remote -v                                    # origin = fc-de/dokdok-server 확인
```

## 1단계: 필수 Read (생략 불가)
- `.github/PULL_REQUEST_TEMPLATE.md` — PR 본문은 이 구조를 그대로 채운다

## 2단계: 브랜치·커밋 상태 점검
```bash
git branch --show-current
git status --short            # 워킹트리 깨끗해야 함
git log dev..HEAD --oneline   # dev 대비 커밋
git diff dev...HEAD --stat    # 변경 파일·규모
```
확인 사항(하나라도 어긋나면 진행 전 처리):
- **워킹트리에 미커밋 변경이 있으면** 먼저 커밋하거나 stash. (섞인 채 PR 금지)
- **현재 브랜치가 `dev`이면 중단** — feature 브랜치에서만 PR. (`git switch -c <type>/<요약>` 안내)
- dev 대비 0커밋이면 올릴 변경 없음 → 중단.

## 3단계: 푸시
각자 로컬 feature 브랜치를 **조직 레포(origin = fc-de/dokdok-server)로 직접 push** 한다. (개인 포크 사용 안 함)
```bash
git push -u origin "$(git branch --show-current)"
```

## 4단계: 연결 이슈 + 변경 유형 결정
- 관련 이슈 번호 확인. 없으면 사용자에게 1줄로 물어본다(없을 수도 있음 — 그땐 번호 생략).
- 변경 유형 → 제목 태그 + 라벨:

| 유형 | 제목 태그 | 라벨 |
|------|----------|------|
| 기능 | `[FEAT]` | `type:feat` |
| 버그 | `[FIX]` | `type:fix` |
| 리팩토링 | `[REFACTOR]` | `type:refactor` |
| 설정/환경 | `[CHORE]` | `type:chore` |
| 테스트 | `[TEST]` | `type:test` |

도메인 라벨(해당 시): `domain:meeting` `domain:book` `domain:topic` `domain:gathering` `domain:retrospective` `domain:users` `domain:keyword` `domain:topic_like` `domain:gathering_member` `domain:meeting_member` `domain:reading_summary`

## 5단계: 제목·본문 작성

**제목**(레포 컨벤션): `[#이슈번호][FIX] <요약>` — 이슈 없으면 `[FIX] <요약>`. 70자 이내, 동사 시작 권장.
예) `[#440][FIX] 확정 대기 약속 거절 시 500 오류 수정`

**본문**(PULL_REQUEST_TEMPLATE 채움 — 체크는 `[x]`):
```markdown
## PR 요약
> <이 PR이 무엇을 하는지 한두 문장>

- [x] 기능 추가 / [x] 버그 수정 ...  ← 해당 항목만 x

---

## 이슈 번호
- #<이슈번호>

---

## 주요 변경 사항
- `파일/모듈`: 변경 내용
- `파일/모듈`: 수정 이유

---

## 참고 사항
- 테스트 방법 / 관련 API / 마이그레이션 필요 여부 등
```
- 이슈를 자동 닫으려면 본문에 `Closes #<번호>` 추가(선택).

## 6단계: 생성 전 확인 (HITL — 자동 생성 절대 금지)
**제목 · base(dev) · head(브랜치) · 라벨 · 본문 전체**를 사용자에게 보여주고 **"이대로 PR 생성할까요?"** 명시 확인.
- Draft 로 올릴지 여부도 확인(기본 Ready). 사용자가 "초안/WIP/스캐폴딩"이라 하면 `--draft`.
- 승인 전 7단계 금지.

## 7단계: 생성
```bash
cat > /tmp/pr-body.md <<'EOF'
<5단계 본문 그대로>
EOF

gh pr create \
  --repo fc-de/dokdok-server \
  --base dev \
  --head "$(git branch --show-current)" \
  --title "[#440][FIX] <요약>" \
  --label "type:fix,domain:meeting" \
  --body-file /tmp/pr-body.md
  # 초안이면 --draft 추가
```
head·base 모두 같은 레포(fc-de/dokdok-server) 내 브랜치다 — cross-repo(포크) PR 아님.

생성 후 출력된 **PR URL을 사용자에게 보고**.

## 주의
- 존재하지 않는 라벨은 생성 실패 → 4단계 목록의 라벨만 사용.
- base 는 항상 `dev`(레포 기본 브랜치). main/master 아님.
- 머지·승인은 이 스킬 범위 밖(리뷰어 몫). PR 생성까지만.
