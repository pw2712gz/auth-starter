import {inject, Injectable} from '@angular/core';
import {AuthService} from './auth.service';
import {UserStoreService} from '../../features/auth/store/user-store.service';
import {TokenUtilsService} from '../../shared/services/token-utils.service';

@Injectable({providedIn: 'root'})
export class UserSessionService {
  private authService = inject(AuthService);
  private userStore = inject(UserStoreService);
  private tokenUtils = inject(TokenUtilsService);

  initUserSession(): void {
    const token = localStorage.getItem('access_token');

    if (!token || this.tokenUtils.isTokenExpired(token)) {
      console.warn('[UserSession] Invalid or expired token. Logging out.');
      this.userStore.logout();
      return;
    }

    this.authService.getCurrentUser().subscribe({
      next: (user) => {
        this.userStore.currentUser.set(user);
        console.info('[UserSession] ✅ Session restored:', user);
      },
      error: (err) => {
        console.error('[UserSession] ❌ Failed to fetch user. Logging out.', err);
        this.userStore.logout();
      },
    });
  }
}
