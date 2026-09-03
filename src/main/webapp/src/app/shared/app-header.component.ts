import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../core/services/auth.service';
import { ThemeService } from '../core/services/theme.service';

/**
 * App-Shell-Header mit Brand-Gradient: links das Logo, rechts Theme-Umschalter,
 * User-Identität und Logout. Unterhalb des Gradient-Streifens läuft eine
 * schmale Sekundär-Navigationsleiste, die pro Rolle Einträge zeigt.
 */
@Component({
  selector: 'app-app-header',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './app-header.component.html',
})
export class AppHeaderComponent {
  protected readonly auth = inject(AuthService);
  protected readonly theme = inject(ThemeService);
}
