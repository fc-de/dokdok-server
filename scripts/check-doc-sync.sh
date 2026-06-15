#!/usr/bin/env bash
# 산출물 동기화 검사 (BE-rules B14) — CI/로컬 공용.
#   B14-1) *ErrorCode.java 변경 시 docs/ErrorCode.md 동반 변경
#   B14-2) *Controller.java 변경 시 대응 *Api.java(Swagger) 동반 변경
# 비교 기준: 첫 인자(base ref), 없으면 origin/dev → dev.
# 위반 시 exit 1 (CI fail). 위반 없으면 exit 0.
#
# 사용:  bash scripts/check-doc-sync.sh [base-ref]
set -uo pipefail

base="${1:-}"
if [ -z "$base" ]; then
  if git rev-parse --verify -q origin/dev >/dev/null 2>&1; then base="origin/dev"; else base="dev"; fi
fi

if ! git rev-parse --verify -q "$base" >/dev/null 2>&1; then
  echo "ℹ️  base ref '$base' 없음 — 검사 건너뜀."; exit 0
fi

changed="$(git diff --name-only "${base}...HEAD" 2>/dev/null)"
[ -z "$changed" ] && { echo "✅ 변경 없음."; exit 0; }

violations=0

# B14-1
if printf '%s\n' "$changed" | grep -Eq 'ErrorCode\.java$'; then
  if ! printf '%s\n' "$changed" | grep -qx 'docs/ErrorCode.md'; then
    echo "❌ B14-1: 에러코드(*ErrorCode.java) 변경됨 → docs/ErrorCode.md 동기화 필요"
    violations=$((violations+1))
  fi
fi

# B14-2
while IFS= read -r f; do
  case "$f" in
    *Controller.java)
      api="$(printf '%s' "$f" | sed -e 's#/controller/#/api/#' -e 's#Controller\.java$#Api.java#')"
      if [ -f "$api" ] && ! printf '%s\n' "$changed" | grep -qx "$api"; then
        echo "❌ B14-2: ${f##*/} 변경됨 → ${api##*/}(Swagger 인터페이스) 갱신 필요"
        violations=$((violations+1))
      fi
      ;;
  esac
done <<< "$changed"

if [ "$violations" -gt 0 ]; then
  echo ""
  echo "산출물 동기화 위반 ${violations}건 (BE-rules B14). 문서/인터페이스를 동기화한 뒤 다시 푸시하세요."
  exit 1
fi
echo "✅ 산출물 동기화 OK (B14)."
exit 0
