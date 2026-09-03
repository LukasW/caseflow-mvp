import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CurrentUser } from '../models/user.model';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  function respondWith(user: CurrentUser): void {
    service.loadCurrentUser().subscribe();
    http.expectOne('/api/v1/me').flush(user);
  }

  it('starts without a user', () => {
    expect(service.user()).toBeUndefined();
    expect(service.roles()).toEqual([]);
    expect(service.isStaff()).toBe(false);
  });

  it('exposes roles and staff flag after loading', () => {
    respondWith({ username: 'anna.cm', roles: ['CASE_MANAGER'], authenticated: true });

    expect(service.username()).toBe('anna.cm');
    expect(service.isAuthenticated()).toBe(true);
    expect(service.isStaff()).toBe(true);
    expect(service.hasRole('CASE_MANAGER')).toBe(true);
    expect(service.hasRole('ADMIN')).toBe(false);
  });

  it('treats a role-less identity as non-staff', () => {
    respondWith({ username: 'guest', roles: [], authenticated: true });

    expect(service.isStaff()).toBe(false);
  });
});
