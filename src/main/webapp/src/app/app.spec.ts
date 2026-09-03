import { signal } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';
import { routes } from './app.routes';
import { CurrentUser, STAFF_ROLES } from './core/models/user.model';
import { AuthService } from './core/services/auth.service';

describe('App', () => {
  function createApp(user: CurrentUser | undefined) {
    const authStub: Pick<AuthService, 'user' | 'isStaff' | 'isAuthenticated' | 'hasRole'> = {
      user: signal(user),
      isStaff: signal(user?.roles.some((role) => STAFF_ROLES.includes(role)) ?? false),
      isAuthenticated: signal(user?.authenticated ?? false),
      hasRole: (...allowed) => allowed.some((role) => user?.roles.includes(role) ?? false),
    };
    TestBed.configureTestingModule({
      imports: [App],
      providers: [
        { provide: AuthService, useValue: authStub },
        provideRouter(routes),
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    return fixture;
  }

  it('should create the app', () => {
    expect(createApp(undefined).componentInstance).toBeTruthy();
  });

  it('should render the router outlet', () => {
    const compiled = createApp(undefined).nativeElement as HTMLElement;
    expect(compiled.querySelector('router-outlet')).toBeTruthy();
  });

  it('shows the current user for a Case Manager', () => {
    const compiled = createApp({
      username: 'anna.cm',
      roles: ['CASE_MANAGER'],
      authenticated: true,
    }).nativeElement as HTMLElement;

    const currentUser = compiled.querySelector('[data-testid="current-user"]');
    expect(currentUser?.textContent).toContain('anna.cm');
    expect(currentUser?.textContent).toContain('CASE_MANAGER');
  });

  it('shows the logout link only for an authenticated identity', () => {
    const authenticated = createApp({
      username: 'anna.cm',
      roles: ['CASE_MANAGER'],
      authenticated: true,
    }).nativeElement as HTMLElement;
    expect(authenticated.querySelector('[data-testid="logout-link"]')).toBeTruthy();
  });
});
