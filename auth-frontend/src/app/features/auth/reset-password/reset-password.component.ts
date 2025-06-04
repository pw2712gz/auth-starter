import {Component, inject, OnInit, signal} from '@angular/core';
import {CommonModule, NgOptimizedImage} from '@angular/common';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {FormControl, FormGroup, ReactiveFormsModule, Validators,} from '@angular/forms';
import {HttpClient} from '@angular/common/http';

import {SpinnerComponent} from '../../../shared/components/ui/spinner.component';
import {FormErrorComponent} from '../../../shared/components/ui/form-error.component';
import {environment} from '../../../../environments/environment';

@Component({
  standalone: true,
  selector: 'app-reset-password',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    SpinnerComponent,
    FormErrorComponent,
    NgOptimizedImage,
  ],
  templateUrl: './reset-password.component.html',
})
export class ResetPasswordComponent implements OnInit {
  form = new FormGroup({
    newPassword: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(6)],
    }),
  });

  loading = signal(false);
  submitted = signal(false);
  errorMessage = signal('');
  token = '';

  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);
  private router = inject(Router);

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';

    if (!this.token) {
      this.errorMessage.set('Missing or invalid reset token.');
    }
  }

  submit(): void {
    if (this.form.invalid || !this.token) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    this.http
      .post<{ message: string }>(
        `${environment.apiUrl}/api/auth/reset-password`,
        {
          token: this.token,
          newPassword: this.form.value.newPassword,
        },
      )
      .subscribe({
        next: (res) => {
          console.log('✅ Password reset success:', res.message);
          this.submitted.set(true);
          this.loading.set(false);

          // Redirect to log in after short delay
          setTimeout(() => this.router.navigate(['/login']), 2500);
        },
        error: (err) => {
          const msg = err?.error?.message || 'Unexpected error occurred.';
          console.error('❌ Password reset failed:', msg);
          this.errorMessage.set(msg);
          this.loading.set(false);
        },
      });
  }
}
