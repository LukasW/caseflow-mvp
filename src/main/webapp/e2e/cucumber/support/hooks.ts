import { After, Before, setDefaultTimeout } from '@cucumber/cucumber';
import { chromium } from '@playwright/test';
import { CaseFlowWorld } from './world';

setDefaultTimeout(30_000);

Before(async function (this: CaseFlowWorld) {
  this.browser = await chromium.launch();
  this.context = await this.browser.newContext();
  this.page = await this.context.newPage();
});

After(async function (this: CaseFlowWorld) {
  await this.context?.close();
  await this.browser?.close();
});
