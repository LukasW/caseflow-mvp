# CaseFlow — Frontend

Angular single-page application, served by the Quarkus backend via Quinoa.

## Development server

```bash
ng serve
```

Open `http://localhost:4200/`. `/api` and `/q` requests are proxied to Quarkus on
port 8080 (see `proxy.conf.json`). Usually you run the whole stack with
`./mvnw quarkus:dev` from the project root instead — Quinoa starts this dev server
automatically.

## Building

```bash
ng build
```

Build artifacts land in `dist/caseflow/browser`, which Quinoa packages into the Quarkus app.

## Unit tests

```bash
ng test
```

Runs the [Vitest](https://vitest.dev/) test runner.

## E2E tests (Cucumber.js + Playwright)

```bash
npm run e2e:cucumber
```

Runs the `@E2E`-tagged feature files from `src/test/resources/features/` against
a running Quarkus instance on `http://localhost:8080` (see `e2e/cucumber/support/`).
