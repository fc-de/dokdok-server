#!/usr/bin/env bash
# PreToolUse(Bash) 훅: dev/main/master 브랜치에서 git commit/push 직접 실행 차단.
# 근거: BE-rules B12 — 작업은 feature 브랜치에서만. dev 직접 커밋 금지.
# 차단: exit 2 (+ stderr) → 도구 호출이 막히고 메시지가 모델에 전달됨.
set -uo pipefail

input="$(cat)"
cmd="$(printf '%s' "$input" \
  | python3 -c 'import sys,json; print(json.load(sys.stdin).get("tool_input",{}).get("command",""))' 2>/dev/null || true)"
[ -z "$cmd" ] && exit 0

# 같은 명령 세그먼트(&|; 로 끊기 전) 안에 git ... commit|push 가 있으면 git 커밋/푸시로 간주
if printf '%s' "$cmd" | grep -Eq '\bgit\b[^&|;]*\b(commit|push)\b'; then
  branch="$(git -C "${CLAUDE_PROJECT_DIR:-.}" rev-parse --abbrev-ref HEAD 2>/dev/null || echo '')"
  case "$branch" in
    dev|main|master)
      echo "⛔ '$branch' 브랜치에 git commit/push 직접 실행 금지 (BE-rules B12)." >&2
      echo "   feature 브랜치에서 작업하세요:  git switch -c <type>/<요약>" >&2
      exit 2
      ;;
  esac
fi
exit 0
