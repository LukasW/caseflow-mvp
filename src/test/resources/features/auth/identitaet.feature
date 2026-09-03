# language: de
Funktionalität: Identität und Rollen des angemeldeten Nutzers

  Als Case Manager
  möchte ich, dass die Anwendung meine Identität und Rollen kennt,
  damit mir die passenden Sichten und Aktionen angezeigt werden.

  Szenario: Angemeldeter Case Manager erhält seine Rolle
    Angenommen ich bin als "anna.cm" mit der Rolle "CASE_MANAGER" angemeldet
    Wenn ich "/api/v1/me" abrufe
    Dann erhalte ich den Status 200
    Und enthält die Antwort die Rolle "CASE_MANAGER"

  Szenario: Version ist nur angemeldet abrufbar
    Angenommen ich bin nicht angemeldet
    Wenn ich "/api/v1/version" abrufe
    Dann erhalte ich den Status 401
