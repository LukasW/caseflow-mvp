import { DOCUMENT } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';
import { ThemeService } from './theme.service';

describe('ThemeService', () => {
  let doc: Document;

  function create(): ThemeService {
    const service = TestBed.inject(ThemeService);
    TestBed.tick(); // initialen Sync-Effect ausführen
    return service;
  }

  beforeEach(() => {
    localStorage.clear();
    document.documentElement.classList.remove('dark');
    TestBed.configureTestingModule({});
    doc = TestBed.inject(DOCUMENT);
  });

  it('startet ohne gespeicherte Auswahl im hellen Modus', () => {
    const theme = create();

    expect(theme.theme()).toBe('light');
    expect(theme.isDark()).toBe(false);
    expect(doc.documentElement.classList.contains('dark')).toBe(false);
  });

  it('übernimmt eine gespeicherte dunkle Auswahl beim Start', () => {
    localStorage.setItem('caseflow-theme', 'dark');

    const theme = create();

    expect(theme.theme()).toBe('dark');
    expect(doc.documentElement.classList.contains('dark')).toBe(true);
  });

  it('schaltet per toggle auf dunkel und persistiert die Auswahl', () => {
    const theme = create();

    theme.toggle();
    TestBed.tick();

    expect(theme.theme()).toBe('dark');
    expect(theme.isDark()).toBe(true);
    expect(doc.documentElement.classList.contains('dark')).toBe(true);
    expect(localStorage.getItem('caseflow-theme')).toBe('dark');
  });

  it('schaltet per toggle wieder auf hell zurück', () => {
    localStorage.setItem('caseflow-theme', 'dark');
    const theme = create();

    theme.toggle();
    TestBed.tick();

    expect(theme.theme()).toBe('light');
    expect(doc.documentElement.classList.contains('dark')).toBe(false);
    expect(localStorage.getItem('caseflow-theme')).toBe('light');
  });

  it('persistiert eine explizit gesetzte Auswahl', () => {
    const theme = create();

    theme.setTheme('dark');
    TestBed.tick();

    expect(theme.isDark()).toBe(true);
    expect(localStorage.getItem('caseflow-theme')).toBe('dark');
  });
});
