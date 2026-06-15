# 자동 강제 (훅)

규칙 중 일부는 `.claude/hooks/` 스크립트로 **시스템이 자동 강제**한다. 문서(dev-rules)는 권고, 훅은 차단이다.

- 등록(배선): `.claude/settings.json` — "어떤 이벤트 + 어떤 도구일 때 → 어떤 스크립트를 돌릴지"
- 판단 로직: `.claude/hooks/*.sh` — stdin 으로 JSON 받아 종료코드로 결정 (`exit 0` 통과 / `exit 2` 차단)
- 적용 시점: **세션 시작 시 1회 로드**(스냅샷). 변경은 새 세션부터 반영(보안상 세션 중 자동 반영 안 함).

## 등록된 훅

| 훅 | 이벤트 / 발동 | 동작 | 근거 |
|----|--------------|------|------|
| `block-dev-commit.sh` | PreToolUse(Bash) — `git commit`·`push` 직접 실행 시 | `dev`/`main`/`master` 브랜치면 **차단**(exit 2) | B12 |

> **산출물 동기화(B14)는 훅으로 두지 않는다.** 매 Bash 명령마다 훅이 도는 낭비를 피하려고, PR 생성 시점에 `/PR생성` 스킬의 4.5단계(B14 자가 점검)에서 한 번만 검사한다.

## 차단됐을 때
- **dev 직접 커밋 차단**: feature 브랜치로 옮긴다 — `git switch -c <type>/<요약>`.

## 새 훅 추가 방법
1. `.claude/hooks/<이름>.sh` 작성 — stdin JSON 파싱, `exit 0/2` 로 결정.
2. `chmod +x` 후 `.claude/settings.json` 의 해당 이벤트에 `command` 등록.
3. 시뮬레이션 검증: `echo '<stdin JSON>' | .claude/hooks/<이름>.sh; echo $?`
4. 라이브 검증은 **새 세션**에서.

> 훅 vs 스킬 판단 기준: **매 명령/이벤트마다 판단이 필요하면 훅**(예: 브랜치 보호), **특정 작업 한 시점에만 필요하면 스킬 단계**(예: PR 시 산출물 동기화).
