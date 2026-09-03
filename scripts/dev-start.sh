#!/usr/bin/env bash
#
# Startet CaseFlow lokal im Quarkus-Dev-Modus (Backend :8080 + Angular via
# Quinoa). Kapselt die Podman-Vorbereitung, die Dev Services (PostgreSQL) auf
# dieser Maschine brauchen.
#
# Optional startet das Skript einen lokalen Keycloak-Container und fährt die App
# im echten OIDC-Login-Flow hoch (--auth keycloak).
#
# Usage:
#   scripts/dev-start.sh [--role cm|lead|admin|auditor] [--auth none|keycloak]
#
# Flags:
#   --role cm|lead|admin|auditor
#                    Ersatzidentität im OIDC-aus-Modus (Default: cm → CASE_MANAGER).
#                    Bei --auth keycloak ohne Wirkung (echter Login).
#   --auth none|keycloak
#                    none (Default): OIDC aus, Dev-Role-Stub, kein Login.
#                    keycloak: startet Keycloak-Container (:8180) und fährt die
#                    App im Profil dev,keycloak mit echtem Login hoch.
#   -h, --help       Diese Hilfe.
#
# Env-Overrides:
#   KEYCLOAK_IMAGE   Keycloak-Container-Image (Default: quay.io/keycloak/keycloak:26.6.4)
#   PODMAN_MACHINE   Name der Podman-Machine (Default: automatisch erkannt)

set -euo pipefail

# --- Pfade -----------------------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
REALM_FILE="${REPO_ROOT}/keycloak/caseflow-realm-dev.json"

# --- Defaults --------------------------------------------------------------
ROLE="cm"
AUTH="none"
# 26.6.x läuft stabil auf der Podman/ARM-VM; ältere Tags crashen dort mit SIGILL.
KEYCLOAK_IMAGE="${KEYCLOAK_IMAGE:-quay.io/keycloak/keycloak:26.6.4}"
KEYCLOAK_CONTAINER="caseflow-keycloak"
KEYCLOAK_PORT="8180"

# --- Ausgabe-Helfer --------------------------------------------------------
info()  { printf '\033[1;34m▶ %s\033[0m\n' "$*"; }
ok()    { printf '\033[1;32m✔ %s\033[0m\n' "$*"; }
warn()  { printf '\033[1;33m⚠ %s\033[0m\n' "$*"; }
die()   { printf '\033[1;31m✖ %s\033[0m\n' "$*" >&2; exit 1; }

usage() { sed -n '2,/^set -euo/p' "${BASH_SOURCE[0]}" | sed '$d;s/^# \{0,1\}//'; exit 0; }

# --- Argumente -------------------------------------------------------------
while [[ $# -gt 0 ]]; do
  case "$1" in
    --role)   ROLE="${2:-}"; shift 2 ;;
    --auth)   AUTH="${2:-}"; shift 2 ;;
    -h|--help) usage ;;
    *) die "Unbekanntes Argument: $1 (siehe --help)" ;;
  esac
done

case "${ROLE}" in
  cm)      DEV_ROLE="CASE_MANAGER" ;;
  lead)    DEV_ROLE="TEAM_LEAD" ;;
  admin)   DEV_ROLE="ADMIN" ;;
  auditor) DEV_ROLE="AUDITOR" ;;
  *)  die "--role muss cm, lead, admin oder auditor sein (war: ${ROLE})" ;;
esac
[[ "${AUTH}" == "none" || "${AUTH}" == "keycloak" ]] || die "--auth muss none oder keycloak sein (war: ${AUTH})"

# --- Podman-Umgebung -------------------------------------------------------
setup_podman_env() {
  if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
    ok "Docker-Daemon erreichbar — Dev Services nutzen Docker."
    return 0
  fi
  command -v podman >/dev/null 2>&1 || die "Weder Docker noch podman gefunden. Container-Runtime für Dev Services benötigt."

  local machine="${PODMAN_MACHINE:-}"
  if [[ -z "${machine}" ]]; then
    machine="$(podman machine list --format '{{.Name}}' 2>/dev/null | sed 's/\*$//' | head -1)"
  fi
  [[ -n "${machine}" ]] || die "Keine Podman-Machine gefunden. Erst 'podman machine init' ausführen."

  if ! podman machine inspect "${machine}" --format '{{.State}}' 2>/dev/null | grep -q running; then
    info "Podman-Machine '${machine}' ist nicht aktiv — starte sie …"
    podman machine start "${machine}" || die "Konnte Podman-Machine '${machine}' nicht starten."
  fi

  local socket
  socket="$(podman machine inspect "${machine}" --format '{{.ConnectionInfo.PodmanSocket.Path}}' 2>/dev/null)"
  [[ -n "${socket}" ]] || die "Konnte Podman-Socket-Pfad nicht ermitteln."

  export DOCKER_HOST="unix://${socket}"
  export TESTCONTAINERS_RYUK_DISABLED=true

  ok "Podman-Umgebung gesetzt (machine=${machine})"
}

# --- Keycloak --------------------------------------------------------------
start_keycloak() {
  [[ -f "${REALM_FILE}" ]] || die "Realm-Datei fehlt: ${REALM_FILE}"

  if podman container exists "${KEYCLOAK_CONTAINER}" 2>/dev/null; then
    if [[ "$(podman inspect -f '{{.State.Running}}' "${KEYCLOAK_CONTAINER}" 2>/dev/null)" == "true" ]]; then
      ok "Keycloak-Container '${KEYCLOAK_CONTAINER}' läuft bereits — wiederverwendet."
    else
      info "Starte vorhandenen Keycloak-Container '${KEYCLOAK_CONTAINER}' neu …"
      podman start "${KEYCLOAK_CONTAINER}" >/dev/null
    fi
  else
    info "Starte Keycloak (${KEYCLOAK_IMAGE}) auf :${KEYCLOAK_PORT} mit Realm-Import …"
    podman run -d --name "${KEYCLOAK_CONTAINER}" \
      -p "${KEYCLOAK_PORT}:8080" \
      -e KEYCLOAK_ADMIN=admin \
      -e KEYCLOAK_ADMIN_PASSWORD=admin \
      -e KC_BOOTSTRAP_ADMIN_USERNAME=admin \
      -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin \
      -v "${REALM_FILE}:/opt/keycloak/data/import/caseflow-realm.json:ro,Z" \
      "${KEYCLOAK_IMAGE}" start-dev --import-realm >/dev/null
  fi

  info "Warte auf Keycloak-Realm 'caseflow' …"
  local url="http://localhost:${KEYCLOAK_PORT}/realms/caseflow/.well-known/openid-configuration"
  for _ in $(seq 1 60); do
    if curl -fsS "${url}" >/dev/null 2>&1; then
      ok "Keycloak bereit: http://localhost:${KEYCLOAK_PORT} (Admin: admin/admin)"
      return 0
    fi
    sleep 2
  done
  die "Keycloak wurde nicht rechtzeitig bereit. Logs: podman logs ${KEYCLOAK_CONTAINER}"
}

# --- Start ------------------------------------------------------------------
setup_podman_env

MVN_ARGS=(quarkus:dev)

if [[ "${AUTH}" == "keycloak" ]]; then
  start_keycloak
  MVN_ARGS+=(-Dquarkus.profile=dev,keycloak)
  info "Modus: OIDC-Login via Keycloak. Testuser (Passwort = Benutzername):"
  printf '    %-13s → %s\n' case-manager CASE_MANAGER team-lead TEAM_LEAD admin ADMIN auditor AUDITOR
else
  MVN_ARGS+=("-Dcaseflow.auth.dev-role=${DEV_ROLE}")
  info "Modus: OIDC aus, Ersatzrolle ${DEV_ROLE} (--role cm|lead|admin|auditor zum Wechseln)."
fi

ok "Starte CaseFlow → http://localhost:8080  (Stoppen mit Ctrl-C)"
[[ "${AUTH}" == "keycloak" ]] && info "Keycloak-Container bleibt laufen. Stoppen: podman stop ${KEYCLOAK_CONTAINER}"

cd "${REPO_ROOT}"
exec ./mvnw "${MVN_ARGS[@]}"
