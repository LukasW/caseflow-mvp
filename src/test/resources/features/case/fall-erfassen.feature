# language: de
Funktionalität: US-1 Fall erfassen

  Als Case Manager
  möchte ich einen neuen Fall mit seinen Grunddaten erfassen,
  damit ein eingehendes Anliegen strukturiert im Tool statt in E-Mail oder Excel liegt.

  Szenario: Fall mit vollständigen Grunddaten erfassen
    Angenommen ich bin als "anna.cm" mit der Rolle "CASE_MANAGER" angemeldet
    Wenn ich einen Fall mit Falltyp "LEISTUNG", Priorität "HOCH", Quelle "TELEFON" und Kundenreferenz "KND-4711" erfasse
    Dann erhalte ich beim Erfassen den Status 201
    Und der Fall trägt eine Fallnummer und den Status "NEU"
    Und der Audit-Trail enthält den Eintrag "CASE_CREATED"
    Und der Audit-Eintrag nennt den Akteur "anna.cm" mit gesetztem Zeitpunkt

  Szenario: Fall ohne Falltyp wird abgewiesen
    Angenommen ich bin als "anna.cm" mit der Rolle "CASE_MANAGER" angemeldet
    Wenn ich einen Fall ohne Falltyp mit Priorität "HOCH", Quelle "TELEFON" und Kundenreferenz "KND-8002" erfasse
    Dann erhalte ich beim Erfassen den Status 400
    Und die Rückmeldung nennt das fehlende Pflichtfeld "Falltyp"
    Und es wurde kein Fall mit der Kundenreferenz "KND-8002" angelegt

  Szenario: Auditor darf keinen Fall erfassen
    Angenommen ich bin als "rita.audit" mit der Rolle "AUDITOR" angemeldet
    Wenn ich einen Fall mit Falltyp "LEISTUNG", Priorität "HOCH", Quelle "TELEFON" und Kundenreferenz "KND-8003" erfasse
    Dann erhalte ich beim Erfassen den Status 403
    Und es wurde kein Fall mit der Kundenreferenz "KND-8003" angelegt
