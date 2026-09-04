import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Observable, of, throwError } from 'rxjs';
import { CaseResponse, CreateCaseRequest } from '../../core/models/case.model';
import { CaseService } from '../../core/services/case.service';
import { CaseCaptureComponent } from './case-capture.component';

describe('CaseCaptureComponent', () => {
  let fixture: ComponentFixture<CaseCaptureComponent>;
  let createCase: (request: CreateCaseRequest) => Observable<CaseResponse>;

  const validCase: CreateCaseRequest = {
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

  function setup() {
    const serviceStub: Pick<CaseService, 'createCase'> = {
      createCase: (request) => createCase(request),
    };
    TestBed.configureTestingModule({
      imports: [CaseCaptureComponent],
      providers: [{ provide: CaseService, useValue: serviceStub }],
    });
    fixture = TestBed.createComponent(CaseCaptureComponent);
    fixture.detectChanges();
  }

  function element<T extends HTMLElement>(testId: string): T | null {
    return (fixture.nativeElement as HTMLElement).querySelector<T>(`[data-testid="${testId}"]`);
  }

  function form() {
    return (fixture.componentInstance as unknown as { form: { setValue: (v: unknown) => void } })
      .form;
  }

  it('does not submit and shows field errors when required fields are missing', () => {
    let called = false;
    createCase = () => {
      called = true;
      return of(response);
    };
    setup();

    element<HTMLButtonElement>('submit-case')!.click();
    fixture.detectChanges();

    expect(called).toBe(false);
    expect(element('error-case-type')).toBeTruthy();
    expect(element('case-success')).toBeFalsy();
  });

  it('creates the case, shows the case number and clears the form on success', () => {
    createCase = () => of(response);
    setup();

    form().setValue(validCase);
    element<HTMLButtonElement>('submit-case')!.click();
    fixture.detectChanges();

    const success = element('case-success');
    expect(success?.textContent).toContain('CASE-000001');
    expect(element<HTMLInputElement>('field-case-reference')!.value).toBe('');
  });

  it('shows the backend validation message on a 400 response', () => {
    createCase = () =>
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            error: { violations: [{ field: 'case.caseType', message: 'Falltyp ist erforderlich' }] },
          }),
      );
    setup();

    form().setValue(validCase);
    element<HTMLButtonElement>('submit-case')!.click();
    fixture.detectChanges();

    expect(element('case-error')?.textContent).toContain('Falltyp ist erforderlich');
    expect(element('case-success')).toBeFalsy();
  });
});
