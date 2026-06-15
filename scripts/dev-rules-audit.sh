#!/usr/bin/env bash
# 컨벤션 준수율 감사 (BE-rules) — repo 전체 스냅샷 측정.
# "통제"의 증거: 재현 가능한 준수율 숫자. 기본은 리포트(exit 0).
#   --strict 면 미준수 1건이라도 있으면 exit 1.
#
# 사용:  bash scripts/dev-rules-audit.sh [--strict]
set -uo pipefail
strict=0; [ "${1:-}" = "--strict" ] && strict=1
SRC="src/main/java/com/dokdok"
fail=0

pct() { # pct <ok> <total>
  if [ "$2" -eq 0 ]; then echo "n/a"; else echo "$(( $1 * 100 / $2 ))%"; fi
}

echo "================ dev-rules 준수율 감사 ================"

# B2: 컨트롤러가 대응 XxxApi 인터페이스를 구현하는가
ctrl_total=0; ctrl_ok=0; ctrl_missing=""
while IFS= read -r f; do
  [ -z "$f" ] && continue
  ctrl_total=$((ctrl_total+1))
  api="$(printf '%s' "$f" | sed -e 's#/controller/#/api/#' -e 's#Controller\.java$#Api.java#')"
  if [ -f "$api" ]; then ctrl_ok=$((ctrl_ok+1)); else ctrl_missing="${ctrl_missing} ${f##*/}"; fi
done < <(find "$SRC" -name '*Controller.java' 2>/dev/null)
echo "B2  컨트롤러 ↔ Swagger 인터페이스 : ${ctrl_ok}/${ctrl_total} ($(pct $ctrl_ok $ctrl_total))"
[ -n "$ctrl_missing" ] && { echo "    누락:${ctrl_missing}"; fail=$((fail+1)); }

# B14-1: ErrorCode enum 코드가 docs/ErrorCode.md 에 문서화됐는가 (코드 단위)
doc="docs/ErrorCode.md"
code_total=0; code_doc=0; code_missing=""
if [ -f "$doc" ]; then
  while IFS= read -r code; do
    code_total=$((code_total+1))
    if grep -q "\b${code}\b" "$doc"; then code_doc=$((code_doc+1)); else code_missing="${code_missing} ${code}"; fi
  done < <(grep -rhoE '"[A-Z][0-9]{3}"' "$SRC" --include='*ErrorCode.java' 2>/dev/null | tr -d '"' | sort -u)
fi
echo "B14 에러코드 문서화(ErrorCode.md)  : ${code_doc}/${code_total} ($(pct $code_doc $code_total))"
[ -n "$code_missing" ] && { echo "    미문서화:${code_missing}"; fail=$((fail+1)); }

# B10: @Enumerated(STRING) 사용 enum 컬럼 수 (CHECK 제약 관리 대상 — 참고 지표)
enum_cols=$(grep -rl 'EnumType.STRING' "$SRC" --include='*.java' 2>/dev/null | wc -l | tr -d ' ')
echo "B10 @Enumerated(STRING) 보유 파일  : ${enum_cols} (값 추가 시 DB CHECK 제약 갱신 대상)"

echo "------------------------------------------------------"
if [ "$fail" -eq 0 ]; then
  echo "✅ 준수율 점검 통과 (미준수 0)"
else
  echo "⚠️  미준수 영역 ${fail}건 — 위 누락 항목 확인"
fi
echo "======================================================"

[ "$strict" -eq 1 ] && [ "$fail" -gt 0 ] && exit 1
exit 0
