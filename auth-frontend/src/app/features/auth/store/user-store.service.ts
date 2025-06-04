import {Injectable, signal, WritableSignal} from '@angular/core';
import {MeResponse} from '../../../shared/models/auth.model';

@Injectable({providedIn: 'root'})
export class UserStoreService {
  isLoggedIn: WritableSignal<boolean> = signal(!!localStorage.getItem('access_token'));
  currentUser: WritableSignal<MeResponse | null> = signal(null);

  login(token: string, refresh: string): void {
    localStorage.setItem('access_token', token);
    localStorage.setItem('refresh_token', refresh);
    this.isLoggedIn.set(true);
    console.info('[UserStore] ✅ Login successful');
  }

  logout(): void {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    this.isLoggedIn.set(false);
    this.currentUser.set(null);
    console.info('[UserStore] 🔒 Logged out and session cleared');
  }
}
