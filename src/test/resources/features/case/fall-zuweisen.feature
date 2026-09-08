# language: de
Funktionalität: US-2 Fall zuweisen und umverteilen

  Als Team Lead
  möchte ich einen Fall einer Person zuweisen und bei Bedarf umverteilen,
  damit die Arbeit im Team klar verteilt ist und kein Fall ohne Zuständigkeit bleibt.

  Szenario: Fall erstmalig zuweisen
    Angenommen ich bin als "tom.tl" mit der Rolle "TEAM_LEAD" angemeldet
    Und ein erfasster Fall ohne Zuständigkeit
    Wenn ich den Fall "bruno.cm" zuweise
    Dann erhalte ich bei der Zuweisung den Status 200
    Und der Fall ist "bruno.cm" zugewiesen
    Und der Audit-Trail enthält "CASE_ASSIGNED" mit Vorher "" und Nachher "bruno.cm"

  Szenario: Zugewiesenen Fall umverteilen
    Angenommen ich bin als "tom.tl" mit der Rolle "TEAM_LEAD" angemeldet
    Und ein Fall, der "bruno.cm" zugewiesen ist
    Wenn ich den Fall "clara.cm" zuweise
    Dann erhalte ich bei der Zuweisung den Status 200
    Und der Fall ist "clara.cm" zugewiesen
    Und der Audit-Trail enthält "CASE_ASSIGNED" mit Vorher "bruno.cm" und Nachher "clara.cm"

  Szenario: Case Manager darf nicht zuweisen
    Angenommen ich bin als "anna.cm" mit der Rolle "CASE_MANAGER" angemeldet
    Und ein erfasster Fall ohne Zuständigkeit
    Wenn ich den Fall "bruno.cm" zuweise
    Dann erhalte ich bei der Zuweisung den Status 403
    Und die Zuständigkeit des Falls bleibt unverändert
