import {Component, Input} from '@angular/core';
import {AbstractControl, FormGroup} from '@angular/forms';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-form-error',
  standalone: true,
  imports: [NgIf],
  template: `
    <p *ngIf="message" class="text-sm text-red-600 dark:text-red-400 mt-1">
      {{ message }}
    </p>
  `,
})
export class FormErrorComponent {
  @Input() form?: FormGroup;
  @Input() controlName?: string;
  @Input() error?: string;

  get message(): string | null {
    if (this.error) return this.error;

    const control: AbstractControl | null =
      this.form?.get(this.controlName!) ?? null;
    if (!control || !control.touched || !control.errors) return null;

    if (control.errors['required']) return 'This field is required.';
    if (control.errors['email']) return 'Please enter a valid email address.';
    if (control.errors['minlength'])
      return `Minimum length: ${control.errors['minlength'].requiredLength}`;
    if (control.errors['maxlength'])
      return `Maximum length: ${control.errors['maxlength'].requiredLength}`;

    return 'Invalid field.';
  }
}
