#!/usr/bin/env bash
# postToolUse-Hook für GitHub Copilot: erinnert nach Datei-Edits an den passenden
# Reviewer-Agenten (.github/agents/*.agent.md).
#
# Input: JSON auf stdin. Copilot liefert je nach Oberfläche
#   - camelCase:  { toolName, toolArgs (JSON-String oder Objekt), ... }
#   - snake_case: { tool_name, tool_input (Objekt), ... }
# Output: { "additionalContext": "..." } auf stdout, nur wenn ein Reviewer passt.
# Exit 0 = nicht-blockierend.
#
# jq ist optional: liegt es vor, wird der Dateipfad präzise extrahiert. Fehlt es,
# fällt der Hook auf ein Grep über das Roh-Payload zurück (best effort, kann bei
# pfad-ähnlichem Datei-Inhalt zusätzlich anschlagen — für einen Reminder ok).

set -euo pipefail

payload=$(cat)

# Präzise Pfad-Extraktion nur, wenn jq verfügbar ist.
file_path=""
if command -v jq >/dev/null 2>&1; then
  file_path=$(printf '%s' "$payload" | jq -r '
    def args:
      (.tool_input // .toolArgs // {})
      | if type == "string" then (try fromjson catch {}) else . end;
    args | (.path // .file_path // .filePath // .filepath // empty)
  ' 2>/dev/null || true)
fi

# Ohne präzisen Pfad das gesamte Payload als Heuristik-Basis nehmen.
haystack="${file_path:-$payload}"

reminders=()

case "$haystack" in
  *src/main/java/ch/css/demo/caseflow/*.java*)
    reminders+=("hexagonal-reviewer — Java-Backend geändert, prüfe Ports/Adapters und DDD-Bausteine.")
    ;;
esac

case "$haystack" in
  *src/main/java/ch/css/demo/caseflow/adapter/in/rest/*.java*|*application*.properties*|*keycloak*|*oidc*)
    reminders+=("auth-security-reviewer — REST-Resource oder OIDC/Keycloak-Konfiguration berührt.")
    ;;
esac

case "$haystack" in
  *src/main/webapp/src/app/*.ts*|*src/main/webapp/src/app/*.html*)
    reminders+=("angular-signals-reviewer — Angular-Code geändert, prüfe Signals/OnPush/@if.")
    ;;
esac

case "$haystack" in
  *.feature*)
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

if command -v jq >/dev/null 2>&1; then
  jq -n --arg ctx "$message" '{additionalContext: $ctx}'
else
  # jq-freier Fallback: die Reminder-Texte enthalten nur kontrollierte Zeichen
  # (keine Anführungszeichen, keine Backslashes) — nur Newlines maskieren.
  escaped=${message//$'\n'/\\n}
  printf '{"additionalContext":"%s"}\n' "$escaped"
fi
exit 0
