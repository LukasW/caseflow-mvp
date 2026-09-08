import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CaseResponse, CreateCaseRequest } from '../models/case.model';
import { CaseService } from './case.service';

describe('CaseService', () => {
  let service: CaseService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CaseService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CaseService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('posts the case to the backend and returns the created case', () => {
    const request: CreateCaseRequest = {
      case_type: 'LEISTUNG',
      priority: 'HOCH',
      source: 'TELEFON',
      case_reference: 'KND-4711',
    };
    const response: CaseResponse = {
      id: 'id-1',
      case_number: 'CASE-000001',
      case_reference: 'KND-4711',
      case_type: 'LEISTUNG',
      priority: 'HOCH',
      source: 'TELEFON',
      status: 'NEU',
      created_at: '2026-09-04T10:00:00Z',
    };

    let received: CaseResponse | undefined;
    service.createCase(request).subscribe((result) => (received = result));

    const req = httpMock.expectOne('/api/v1/cases');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(response);

    expect(received).toEqual(response);
  });
});
