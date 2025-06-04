import {Component, Input} from '@angular/core';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-spinner',
  standalone: true,
  imports: [NgIf],
  template: `
    <div class="flex flex-col items-center justify-center gap-4" role="status" aria-live="polite">
      <div
        class="w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"
      ></div>
      <p *ngIf="message" class="text-gray-500 dark:text-gray-400">{{ message }}</p>
    </div>
  `,
})
export class SpinnerComponent {
  @Input() message = 'Loading...';
}
