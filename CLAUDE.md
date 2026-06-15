# CLAUDE.md

dokdok-server 개발 시 Claude 가 따르는 진입점. 상세 규칙은 본문이 아니라 아래 문서를 **읽고** 작업한다.

## 프로젝트
독서모임 서비스 백엔드. **Spring Boot 4 / Java 21 / PostgreSQL**, 도메인형 패키지(`com.dokdok.<도메인>.{api,controller,dto,entity,exception,repository,service}`).

## 개발 규칙 (작업 전 Read)
- **[docs/dev-rules/BE-rules.md](docs/dev-rules/BE-rules.md)** — 백엔드 개발 규칙 B1~B14. 코드 작성/수정 전, PR 전 자가 점검 기준.
- **[docs/dev-rules/HOOKS.md](docs/dev-rules/HOOKS.md)** — 자동 강제(훅) 동작.
- **[docs/dev-rules/README.md](docs/dev-rules/README.md)** — harness 진입점.

## 절대 규칙 (강제됨)
- **`dev`/`main`/`master` 에 직접 `git commit`·`push` 금지** — feature 브랜치에서만. (훅 `block-dev-commit.sh` 가 차단 / B12)
- **enum 값 추가 시** DB CHECK 제약 갱신 SQL(`src/main/resources/data/*.sql`)을 함께 넣는다. 안 하면 운영에서 500. (B10)
- **에러코드/ API 변경 시 산출물 동기화**: `*ErrorCode.java`↔`docs/ErrorCode.md`, `*Controller.java`↔`*Api.java`. (B14, CI 가 검사)
- 외부 작업(이슈/PR 생성)은 **사용자 확인 없이 자동 실행 금지**.

## 스킬
- `/이슈생성` — `.github` 템플릿+라벨로 GitHub 이슈 생성
- `/PR생성` — `.github` PR 템플릿 + BE-rules 자가 점검 기반 PR 생성
- `/테스트작성` — BE-rules §11 기준 테스트 작성(해피+더티 케이스 필수) + 실행

## 자주 쓰는 명령
```bash
./gradlew test                                  # 전체 테스트
./gradlew test --tests "com.dokdok.<...>Test"   # 특정 테스트
./gradlew compileJava                           # 컴파일만
bash scripts/dev-rules-audit.sh                 # 컨벤션 준수율 점검
```

## 검증 우선
QA 버그를 고치면 **재현 → 수정 → 회귀 테스트** 순. 단, 운영 스키마 의존 버그(enum CHECK 등)는 H2 테스트로 재현 안 됨을 인지(B10).
