import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthRedirect } from '../services/auth-redirect.service';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpController: HttpTestingController;
  const redirectStub = { toLogin: vi.fn() };

  beforeEach(() => {
    redirectStub.toLogin.mockClear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthRedirect, useValue: redirectStub },
      ],
    });
    http = TestBed.inject(HttpClient);
    httpController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpController.verify());

  it('marks requests as AJAX via the X-Requested-With header', () => {
    http.get('/api/v1/cases').subscribe();
    const request = httpController.expectOne('/api/v1/cases');

    expect(request.request.headers.get('X-Requested-With')).toBe('XMLHttpRequest');
    request.flush(null);
  });

  it('redirects to the OIDC login on a 401 response', () => {
    http.get('/api/v1/cases').subscribe({ error: () => undefined });
    httpController
      .expectOne('/api/v1/cases')
      .flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(redirectStub.toLogin).toHaveBeenCalledOnce();
  });

  it('redirects to the OIDC login on a 499 response (AJAX auth required)', () => {
    http.get('/api/v1/cases').subscribe({ error: () => undefined });
    httpController.expectOne('/api/v1/cases').flush(null, { status: 499, statusText: 'OIDC' });

    expect(redirectStub.toLogin).toHaveBeenCalledOnce();
  });

  it('passes other errors through without redirecting', () => {
    http.get('/api/v1/cases').subscribe({ error: () => undefined });
    httpController
      .expectOne('/api/v1/cases')
      .flush(null, { status: 500, statusText: 'Server Error' });

    expect(redirectStub.toLogin).not.toHaveBeenCalled();
  });
});
