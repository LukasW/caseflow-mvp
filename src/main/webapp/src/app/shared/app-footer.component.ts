import { SlicePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AuthService } from '../core/services/auth.service';
import { VersionService } from '../core/services/version.service';

/**
 * Dezenter App-Footer im CSS-Branding-Stil: Copyright links, Versionsinfo
 * und Dokumentations-Link rechts.
 */
@Component({
  selector: 'app-app-footer',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [SlicePipe],
  templateUrl: './app-footer.component.html',
})
export class AppFooterComponent {
  protected readonly auth = inject(AuthService);
  protected readonly versionService = inject(VersionService);
}
