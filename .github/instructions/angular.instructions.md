---
applyTo: "src/main/webapp/src/app/**/*.ts,src/main/webapp/src/app/**/*.html"
---

# Frontend — Angular-Konventionen

CaseFlow nutzt Angular 22 mit modernen Patterns; veraltete Patterns werden abgelehnt.
Nach Änderungen den Custom Agent `angular-signals-reviewer` laufen lassen.

## Components
- Standalone ist Default — **niemals `standalone: true` explizit setzen**, keine NgModules
- `ChangeDetectionStrategy.OnPush` — immer
- Selector-Prefix `app-*`
- Inline-Template nur bei sehr kleinen Komponenten (< 20 Zeilen Template)

## State & Reaktivität
- Signals für allen reaktiven State: `signal()`, `computed()`, `linkedSignal()`, `effect()`
- Kein `BehaviorSubject` für Component-State (nur bei Interop mit RxJS-Libs)
- `input()` / `input.required<T>()` / `output()` / `model()` — nie `@Input()`/`@Output()`

## DI
- `inject()` — nie Constructor Injection in Components/Services
- `providedIn: 'root'` für Singleton-Services

## Templates
- Nativer Control Flow: `@if`, `@for`, `@switch`, `@let` — nie `*ngIf`, `*ngFor`, `*ngSwitch`
- `[class.foo]`/`[style.x]`-Bindings — kein `ngClass`, `ngStyle`

## Forms
- Reactive Forms bevorzugt; Template-driven nur bei trivialen Formularen

## TypeScript
- Strict Mode — kein `any`, `unknown` bei unsicherem Typ
- Return-Types auf allen public Methoden
- Keine `null`-Returns — `undefined` oder Union-Types

## Architektur
- Business-Logik und REST-Zugriffe in `core/services/`, nie in Components
- Domain-Interfaces (spiegeln die REST-DTOs) in `core/models/` — keine Invarianten
- Components in `features/` sind UI-Driver: orchestrieren Services, rendern State
- Wiederverwendbare UI in `shared/` (erst anlegen, wenn nötig)
- Neues Feature: Komponente in `features/`, Route in `app.routes.ts`, Nav-Link in
  `shared/app-header.component.html`

## Auth im Frontend
- HTTP verlässt sich auf das BFF-Session-Cookie — keine manuellen `Authorization`-Header
- Keine Tokens/Secrets in `localStorage`, `sessionStorage` oder im Code
- Guards sind Komfort — Autorisierung entscheidet das Backend

## Sprache & Format
- Sichtbare Strings auf Deutsch mit korrekten Umlauten, Schweizer Hochdeutsch ohne `ß`, Du-Form
- Prettier (print width 100, single quotes), 2 Spaces
- Tests: Vitest (`npm test`), E2E via Cucumber.js/Playwright (`npm run e2e:cucumber`),
  Selektoren via `aria-label`/`data-testid`
