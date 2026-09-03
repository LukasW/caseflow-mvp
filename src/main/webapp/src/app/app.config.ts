import {
  ApplicationConfig,
  LOCALE_ID,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors, withXhr } from '@angular/common/http';
import { registerLocaleData } from '@angular/common';
import localeDeCh from '@angular/common/locales/de-CH';
import { catchError, of } from 'rxjs';

import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { AuthService } from './core/services/auth.service';

// Schweizer Hochdeutsch als Default für date/number/currency-Pipes — ohne Registrierung
// rendert Angular sonst en-US ("Sunday, 31. May" / "CHF 8,500.00").
registerLocaleData(localeDeCh, 'de-CH');

export const appConfig: ApplicationConfig = {
  providers: [
    { provide: LOCALE_ID, useValue: 'de-CH' },
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withXhr(), withInterceptors([authInterceptor])),
    // Rollenkontext vor dem ersten Rendern laden, damit Route-Guards greifen.
    // Schlägt der Aufruf fehl, bootet die App ohne Rolle weiter.
    provideAppInitializer(() =>
      inject(AuthService)
        .loadCurrentUser()
        .pipe(catchError(() => of(undefined))),
    ),
  ],
};
