#!/usr/bin/env bash
# postToolUse-Hook für GitHub Copilot: erinnert nach Datei-Edits an den passenden
# Reviewer-Agenten (.github/agents/*.agent.md).
#
# Input: JSON auf stdin. Copilot liefert je nach Oberfläche
#   - camelCase:  { toolName, toolArgs (JSON-String oder Objekt), ... }
#   - snake_case: { tool_name, tool_input (Objekt), ... }
# Output: { "additionalContext": "..." } auf stdout, nur wenn ein Reviewer passt.
# Exit 0 = nicht-blockierend.

set -euo pipefail

if ! command -v jq >/dev/null 2>&1; then
  exit 0
fi

payload=$(cat)

file_path=$(printf '%s' "$payload" | jq -r '
  def args:
    (.tool_input // .toolArgs // {})
    | if type == "string" then (try fromjson catch {}) else . end;
  args | (.path // .file_path // .filePath // .filepath // empty)
' 2>/dev/null || true)

if [[ -z "$file_path" ]]; then
  exit 0
fi

reminders=()

case "$file_path" in
  *src/main/java/ch/css/demo/caseflow/*.java)
    reminders+=("hexagonal-reviewer — Java-Backend geändert, prüfe Ports/Adapters und DDD-Bausteine.")
    ;;
esac

case "$file_path" in
  *src/main/java/ch/css/demo/caseflow/adapter/in/rest/*.java|*application*.properties|*keycloak*|*oidc*)
    reminders+=("auth-security-reviewer — REST-Resource oder OIDC/Keycloak-Konfiguration berührt.")
    ;;
esac

case "$file_path" in
  *src/main/webapp/src/app/*.ts|*src/main/webapp/src/app/*.html)
    reminders+=("angular-signals-reviewer — Angular-Code geändert, prüfe Signals/OnPush/@if.")
    ;;
esac

case "$file_path" in
  *.feature)
    reminders+=("bdd-cucumber-author — Feature-Datei geändert, prüfe Tag-Routing.")
    ;;
esac

if (( ${#reminders[@]} == 0 )); then
  exit 0
fi

message="[reviewer-reminder] Geänderte Pfade erfordern Review vor dem Commit — starte die Custom Agents:"
for r in "${reminders[@]}"; do
  message+=$'\n'"  - $r"
done

jq -n --arg ctx "$message" '{additionalContext: $ctx}'
exit 0
