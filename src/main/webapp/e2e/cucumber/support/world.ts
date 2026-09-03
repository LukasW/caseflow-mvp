import { setWorldConstructor, World, IWorldOptions } from '@cucumber/cucumber';
import { Browser, BrowserContext, Page } from '@playwright/test';

/**
 * Cucumber-World für die Playwright-E2E-Szenarien: hält Browser, Context
 * und Page pro Szenario. Basis-URL: `E2E_BASE_URL` (Default Quarkus :8080).
 */
export class CaseFlowWorld extends World {
  browser!: Browser;
  context!: BrowserContext;
  page!: Page;
  readonly baseUrl = process.env['E2E_BASE_URL'] ?? 'http://localhost:8080';

  constructor(options: IWorldOptions) {
    super(options);
  }
}

setWorldConstructor(CaseFlowWorld);
