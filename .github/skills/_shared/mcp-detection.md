# Shared Fragment: MCP-Server ermitteln

Führe einmal pro Session aus und halte das Ergebnis im Kontext:

```bash
git remote get-url origin
```

Wähle den Git-Provider-MCP anhand der URL:
- `github.com` → **GitHub MCP** (in Copilot standardmässig verfügbar)
- `gitlab.com` oder self-hosted GitLab → **GitLab MCP**
- Andere (z. B. Gitea) → **Gitea MCP**

Owner und Repo immer aus der Remote-URL ableiten — niemals hardcoden. Ist kein
passender MCP-Server konfiguriert, `gh` (bzw. `glab`/`tea`) im Terminal nutzen.
