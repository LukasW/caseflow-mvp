import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';

/**
 * Platzhalter-Startseite. Wird mit dem ersten Feature (Fallübersicht) ersetzt.
 */
@Component({
  selector: 'app-home',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="mx-auto max-w-5xl px-4 py-10 sm:px-8">
      <h1 class="text-3xl">Willkommen bei CaseFlow</h1>
      <p class="mt-4 text-ink-muted">
        Ein Tool. Ein Fall. Volle Kontrolle. Die Fallübersicht entsteht mit der ersten User Story.
      </p>
      @if (auth.username(); as name) {
        <p class="mt-2 text-sm text-ink-subtle" data-testid="home-greeting">
          Angemeldet als {{ name }}
        </p>
      }
    </section>
  `,
})
export class HomeComponent {
  protected readonly auth = inject(AuthService);
}
