import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  CASE_TYPE_OPTIONS,
  CASE_STATUS_LABELS,
  CaseResponse,
  CreateCaseRequest,
  PRIORITY_OPTIONS,
  SOURCE_OPTIONS,
} from '../../core/models/case.model';
import { CaseService } from '../../core/services/case.service';

/**
 * Erfassungsformular für einen neuen Fall. Sendet die Grunddaten an
 * `POST /api/v1/cases`, zeigt nach Erfolg die vergebene Fallnummer und leert
 * das Formular für die nächste Erfassung. Die verbindliche Validierung und
 * Autorisierung bleibt serverseitig.
 */
@Component({
  selector: 'app-case-capture',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
  templateUrl: './case-capture.component.html',
})
export class CaseCaptureComponent {
  private readonly caseService = inject(CaseService);
  private readonly formBuilder = inject(FormBuilder);

  protected readonly caseTypes = CASE_TYPE_OPTIONS;
  protected readonly priorities = PRIORITY_OPTIONS;
  protected readonly sources = SOURCE_OPTIONS;
  protected readonly statusLabels = CASE_STATUS_LABELS;

  protected readonly form = this.formBuilder.nonNullable.group({
    case_type: this.formBuilder.nonNullable.control('', Validators.required),
    priority: this.formBuilder.nonNullable.control('', Validators.required),
    source: this.formBuilder.nonNullable.control('', Validators.required),
    case_reference: this.formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.maxLength(64),
    ]),
  });

  protected readonly submitting = signal(false);
  protected readonly createdCase = signal<CaseResponse | undefined>(undefined);
  protected readonly errorMessage = signal<string | undefined>(undefined);

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(undefined);

    this.caseService.createCase(this.form.getRawValue() as CreateCaseRequest).subscribe({
      next: (created) => {
        this.createdCase.set(created);
        this.form.reset();
        this.submitting.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.createdCase.set(undefined);
        this.errorMessage.set(this.toMessage(error));
        this.submitting.set(false);
      },
    });
  }

  private toMessage(error: HttpErrorResponse): string {
    const violations = error.error?.violations;
    if (Array.isArray(violations) && violations.length > 0) {
      return violations.map((violation) => violation.message).join(' ');
    }
    return 'Der Fall konnte nicht erfasst werden. Bitte versuche es erneut.';
  }
}
