import {Component, signal} from '@angular/core';
import {CommonModule, NgOptimizedImage} from '@angular/common';
import {RouterLink} from '@angular/router';
import {FormControl, FormGroup, ReactiveFormsModule, Validators,} from '@angular/forms';
import {HttpClient} from '@angular/common/http';

import {SpinnerComponent} from '../../../shared/components/ui/spinner.component';
import {FormErrorComponent} from '../../../shared/components/ui/form-error.component';
import {environment} from '../../../../environments/environment';

@Component({
  standalone: true,
  selector: 'app-forgot-password',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    SpinnerComponent,
    FormErrorComponent,
    NgOptimizedImage,
  ],
  templateUrl: './forgot-password.component.html',
})
export class ForgotPasswordComponent {
  form = new FormGroup({
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
  });

  loading = signal(false);
  submitted = signal(false);
  errorMessage = signal('');

  constructor(private http: HttpClient) {
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    this.http
      .post(`${environment.apiUrl}/api/auth/forgot-password`, this.form.value)
      .subscribe({
        next: () => {
          this.submitted.set(true);
          this.loading.set(false);
        },
        error: (err) => {
          console.error('❌ Forgot password request failed:', err);
          const msg =
            err?.error?.message || 'Something went wrong. Please try again.';
          this.errorMessage.set(msg);
          this.loading.set(false);
        },
      });
  }
}
