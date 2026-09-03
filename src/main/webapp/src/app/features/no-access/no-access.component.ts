import { ChangeDetectionStrategy, Component } from '@angular/core';

/** Wird angezeigt, wenn eine angemeldete Identität keine CaseFlow-Rolle besitzt. */
@Component({
  selector: 'app-no-access',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="mx-auto max-w-5xl px-4 py-10 sm:px-8">
      <h1 class="text-3xl">Kein Zugriff</h1>
      <p class="mt-4 text-ink-muted">
        Deinem Konto ist keine CaseFlow-Rolle zugewiesen. Wende dich an deine Administration.
      </p>
    </section>
  `,
})
export class NoAccessComponent {}
