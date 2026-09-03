import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { VersionInfo } from '../models/version.model';

@Injectable({ providedIn: 'root' })
export class VersionService {
  private readonly http = inject(HttpClient);

  readonly versionInfo = signal<VersionInfo | undefined>(undefined);

  constructor() {
    this.http
      .get<VersionInfo>('/api/v1/version')
      .pipe(takeUntilDestroyed())
      // Die Versionsanzeige ist rein informativ: Schlägt der Abruf fehl, bleibt
      // versionInfo undefined statt eine unbehandelte Fehlermeldung auszulösen.
      .subscribe({ next: (info) => this.versionInfo.set(info), error: () => {} });
  }
}
