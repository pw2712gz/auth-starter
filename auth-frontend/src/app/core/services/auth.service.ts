import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {catchError, Observable, tap, throwError} from 'rxjs';
import {Router} from '@angular/router';

import {
  AuthResponse,
  LoginRequest,
  MeResponse,
  RefreshTokenRequest,
  RegisterRequest,
} from '../../shared/models/auth.model';
import {environment} from '../../../environments/environment';
import {UserStoreService} from '../../features/auth/store/user-store.service';

@Injectable({providedIn: 'root'})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly userStore = inject(UserStoreService);
  private readonly BASE_URL = `${environment.apiUrl}/api/auth`;

  login(payload: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.BASE_URL}/login`, payload).pipe(
      tap((res) => console.log('[AuthService] Login successful:', res)),
      catchError(this.handleError('Login'))
    );
  }

  register(payload: RegisterRequest): Observable<string> {
    return this.http.post(`${this.BASE_URL}/register`, payload, {responseType: 'text'}).pipe(
      tap(() => console.log('[AuthService] Registration successful')),
      catchError(this.handleError('Register'))
    );
  }

  refresh(payload: RefreshTokenRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.BASE_URL}/refresh`, payload).pipe(
      tap(() => console.log('[AuthService] Token refreshed')),
      catchError(this.handleError('Refresh'))
    );
  }

  logout(payload: RefreshTokenRequest): Observable<string> {
    return this.http.post(`${this.BASE_URL}/logout`, payload, {responseType: 'text'}).pipe(
      tap(() => console.log('[AuthService] Logout successful')),
      catchError(this.handleError('Logout'))
    );
  }

  getCurrentUser(): Observable<MeResponse> {
    return this.http.get<MeResponse>(`${this.BASE_URL}/me`).pipe(
      tap((user) => console.log('[AuthService] Current user fetched:', user)),
      catchError(this.handleError('Get Current User'))
    );
  }

  handleLoginSuccess(auth: AuthResponse): void {
    this.userStore.login(auth.authenticationToken, auth.refreshToken);

    this.getCurrentUser().subscribe({
      next: (user) => {
        this.userStore.currentUser.set(user);
        console.log('[AuthService] Current user set:', user);
        void this.router.navigateByUrl('/dashboard').catch((err) =>
          console.error('[AuthService] Navigation error:', err)
        );
      },
      error: (err) => {
        console.warn('[AuthService] Failed to fetch current user after login:', err);
      },
    });
  }

  private handleError(context: string) {
    return (error: any) => {
      console.error(`[AuthService] ${context} error:`, error);
      return throwError(() => error);
    };
  }
}
