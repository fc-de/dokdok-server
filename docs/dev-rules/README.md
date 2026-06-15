# dev-rules — dokdok 개발 규칙 (harness)

Claude 가 dokdok-server 를 개발할 때 **참조하는 문서 기반 규칙 모음**. 효율적이고 일관된 작업을 위한 harness 엔지니어링 구성요소다.

## 문서

| 문서 | 내용 | 언제 읽나 |
|------|------|-----------|
| [BE-rules.md](BE-rules.md) | 백엔드 개발 규칙 B1~B13 (레이어·컨트롤러·예외·엔티티·DTO·리포지토리·enum CHECK·테스트 등) | 코드 작성/수정 전, PR 생성 전 자가 점검 |

> dokdok-server 는 백엔드(Spring) 단일 레포이므로 BE-rules 가 핵심이다. 필요 시 SECURITY/DATA-MODEL 규칙을 분리 추가한다.

## 사용 방식

- **개발 전**: 해당 영역 규칙을 Read 하고 그 기준으로 작성한다.
- **PR 전**: BE-rules 의 "자가 점검 요약" 표로 위반/누락을 점검한다. → `/PR생성` 스킬과 연계.
- **규칙 변경**: 이 문서들은 PR 로만 수정한다(진실의 원천).

## 연계 스킬

- `/이슈생성` — `.github` 템플릿 + 라벨 컨벤션으로 GitHub 이슈 생성
- `/테스트작성` — BE-rules §11 기준 테스트 작성(해피+더티 케이스 필수) + 실행·검증
- `/PR생성` — `.github` PR 템플릿 + BE-rules 자가 점검 기반 PR 생성
