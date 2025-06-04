import {Component, computed, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router} from '@angular/router';

import {SpinnerComponent} from '../../shared/components/ui/spinner.component';
import {AuthService} from '../../core/services/auth.service';
import {UserStoreService} from '../auth/store/user-store.service';
import {ThemeService} from '../../core/services/theme.service';
import {TokenUtilsService} from '../../shared/services/token-utils.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, SpinnerComponent],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent {
  readonly isDarkMode = computed(() =>
    document.documentElement.classList.contains('dark')
  );
  readonly initials = computed(() => {
    const u = this.user();
    if (!u) return '';
    return (
      (u.firstName?.[0] ?? '') + (u.lastName?.[0] ?? '')
    ).toUpperCase();
  });
  readonly fullName = computed(() => {
    const u = this.user();
    if (!u) return '';
    return `${this.capitalize(u.firstName)} ${this.capitalize(u.lastName)}`;
  });
  private userStore = inject(UserStoreService);
  user = this.userStore.currentUser;
  private router = inject(Router);
  private authService = inject(AuthService);
  private themeService = inject(ThemeService);
  private tokenUtils = inject(TokenUtilsService);

  toggleTheme(): void {
    this.themeService.toggleDarkMode();
  }

  onLogout(): void {
    const refreshToken = localStorage.getItem('refresh_token');
    const token = localStorage.getItem('access_token');
    const email = this.tokenUtils.decodeEmailFromJWT(token ?? '') ?? '';

    const redirectToLogin = () =>
      this.router.navigateByUrl('/login').catch((err) =>
        console.error('[Dashboard] Navigation error during logout:', err)
      );

    if (refreshToken && email) {
      this.authService.logout({refreshToken, email}).subscribe({
        next: () => {
          this.userStore.logout();
          void redirectToLogin();
        },
        error: () => {
          this.userStore.logout();
          void redirectToLogin();
        },
      });
    } else {
      this.userStore.logout();
      void redirectToLogin();
    }
  }

  private capitalize(str: string): string {
    return str ? str.charAt(0).toUpperCase() + str.slice(1) : '';
  }
}
