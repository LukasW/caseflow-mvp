import { Given, Then } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import { CaseFlowWorld } from '../support/world';

Given('ich öffne die Startseite', async function (this: CaseFlowWorld) {
  await this.page.goto(this.baseUrl);
});

Then('sehe ich den Titel {string}', async function (this: CaseFlowWorld, title: string) {
  await expect(this.page.getByRole('heading', { level: 1 })).toHaveText(title);
});
