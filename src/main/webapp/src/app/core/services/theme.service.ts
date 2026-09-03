import { DOCUMENT } from '@angular/common';
import { Injectable, computed, effect, inject, signal } from '@angular/core';

export type Theme = 'light' | 'dark';

const STORAGE_KEY = 'caseflow-theme';
const DARK_CLASS = 'dark';

/**
 * Verwaltet das Hell/Dunkel-Theme der Oberfläche als Signal-Kontext (US-480).
 * Die Auswahl wird in `localStorage` persistiert und beim Start wiederhergestellt;
 * ohne gespeicherte Auswahl gilt bewusst der helle Modus — die Betriebssystem-
 * Präferenz wird nicht übernommen. Das `dark`-Flag am Wurzelelement schaltet die
 * in `styles.css` definierten Theme-Tokens um.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly document = inject(DOCUMENT);
  private readonly current = signal<Theme>(this.readStoredTheme());

  /** Aktuell aktives Theme. */
  readonly theme = this.current.asReadonly();

  /** `true`, wenn der Dunkelmodus aktiv ist. */
  readonly isDark = computed(() => this.current() === 'dark');

  constructor() {
    // Hält DOM-Flag und Persistenz synchron zum Signal — deckt auch den
    // initialen Lauf ab, falls das Inline-Bootstrap-Skript das Flag nicht setzte.
    effect(() => this.applyTheme(this.current()));
  }

  /** Schaltet zwischen hellem und dunklem Modus um. */
  toggle(): void {
    this.current.update((theme) => (theme === 'dark' ? 'light' : 'dark'));
  }

  /** Setzt das Theme explizit. */
  setTheme(theme: Theme): void {
    this.current.set(theme);
  }

  private applyTheme(theme: Theme): void {
    this.document.documentElement.classList.toggle(DARK_CLASS, theme === 'dark');
    this.storage()?.setItem(STORAGE_KEY, theme);
  }

  private readStoredTheme(): Theme {
    return this.storage()?.getItem(STORAGE_KEY) === 'dark' ? 'dark' : 'light';
  }

  private storage(): Storage | undefined {
    try {
      return this.document.defaultView?.localStorage ?? undefined;
    } catch {
      // localStorage kann in restriktiven Kontexten (blockierte Cookies) werfen.
      return undefined;
    }
  }
}
