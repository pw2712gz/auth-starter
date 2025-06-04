import {Component, inject, signal} from '@angular/core';
import {CommonModule, NgOptimizedImage} from '@angular/common';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {finalize} from 'rxjs/operators';

import {AuthService} from '../../../core/services/auth.service';
import {RegisterRequest} from '../../../shared/models/auth.model';
import {SpinnerComponent} from '../../../shared/components/ui/spinner.component';
import {FormErrorComponent} from '../../../shared/components/ui/form-error.component';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    SpinnerComponent,
    RouterLink,
    FormErrorComponent,
    NgOptimizedImage,
  ],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  form = new FormGroup({
    firstName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    lastName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(8)],
    }),
  });

  loading = signal(false);
  redirecting = signal(false);
  errorMessage = signal<string | null>(null);

  private authService = inject(AuthService);

  register() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const payload: RegisterRequest = this.form.getRawValue();
    this.loading.set(true);
    this.errorMessage.set(null);

    this.authService.register(payload).subscribe({
      next: () => {
        this.redirecting.set(true);
        this.authService
          .login({email: payload.email, password: payload.password})
          .pipe(finalize(() => this.loading.set(false)))
          .subscribe({
            next: (res) => this.authService.handleLoginSuccess(res),
            error: (err) => {
              this.errorMessage.set(err.error?.message || 'Registered, but login failed.');
              this.redirecting.set(false);
            },
          });
      },
      error: (err) => {
        this.errorMessage.set(err.error?.message || 'Registration failed.');
        this.loading.set(false);
      },
    });
  }
}
