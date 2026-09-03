import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { homeAccessGuard, roleGuard } from './role.guard';

describe('role guards', () => {
  function setup(roles: string[], authenticated = true) {
    const authStub = {
      isStaff: signal(roles.length > 0),
      isAuthenticated: signal(authenticated),
      hasRole: (...allowed: string[]) => allowed.some((role) => roles.includes(role)),
    };
    TestBed.configureTestingModule({
      providers: [{ provide: AuthService, useValue: authStub }],
    });
    return TestBed.inject(Router);
  }

  function run(guard: ReturnType<typeof roleGuard>) {
    return TestBed.runInInjectionContext(() => guard({} as never, { url: '/' } as never));
  }

  it('roleGuard allows a matching role', () => {
    setup(['ADMIN']);
    expect(run(roleGuard('ADMIN'))).toBe(true);
  });

  it('roleGuard redirects to the start page without matching role', () => {
    setup(['CASE_MANAGER']);
    expect(run(roleGuard('ADMIN'))).toBeInstanceOf(UrlTree);
  });

  it('homeAccessGuard sends role-less identities to /no-access', () => {
    const router = setup([]);
    const result = run(homeAccessGuard) as UrlTree;
    expect(router.serializeUrl(result)).toBe('/no-access');
  });
});
