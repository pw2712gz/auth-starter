import {Component, inject, signal} from '@angular/core';
import {CommonModule, NgOptimizedImage} from '@angular/common';
import {FormControl, FormGroup, ReactiveFormsModule, Validators,} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {finalize} from 'rxjs/operators';

import {AuthService} from '../../../core/services/auth.service';
import {LoginRequest} from '../../../shared/models/auth.model';
import {SpinnerComponent} from '../../../shared/components/ui/spinner.component';
import {FormErrorComponent} from '../../../shared/components/ui/form-error.component';
import {environment} from '../../../../environments/environment';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    SpinnerComponent,
    FormErrorComponent,
    NgOptimizedImage,
  ],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  form = new FormGroup({
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  loading = signal(false);
  redirecting = signal(false);
  loginError = signal<string | null>(null);

  private authService = inject(AuthService);

  login() {
    this.loginError.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const payload: LoginRequest = this.form.getRawValue();
    this.loading.set(true);

    if (!environment.production) {
      console.debug('[LoginComponent] Submitting payload:', payload);
    }

    this.authService
      .login(payload)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (res) => {
          this.redirecting.set(true);
          this.authService.handleLoginSuccess(res);
        },
        error: (err) => {
          this.loginError.set(
            err.error?.message || 'Login failed. Please try again.'
          );
        },
      });
  }
}
