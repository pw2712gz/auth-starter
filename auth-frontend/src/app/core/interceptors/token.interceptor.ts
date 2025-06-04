import {HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {catchError, from, switchMap} from 'rxjs';

import {AuthService} from '../services/auth.service';
import {TokenUtilsService} from '../../shared/services/token-utils.service';
import {environment} from '../../../environments/environment';

const publicAuthEndpoints = [
  '/api/auth/login',
  '/api/auth/register',
  '/api/auth/refresh',
];

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const tokenUtils = inject(TokenUtilsService);

  const token = localStorage.getItem('access_token');
  const refreshToken = localStorage.getItem('refresh_token');
  const email = tokenUtils.decodeEmailFromJWT(token ?? '') ?? '';

  const isAuthEndpoint = publicAuthEndpoints.some(endpoint =>
    req.url.startsWith(`${environment.apiUrl}${endpoint}`)
  );

  if (!token || !email) {
    if (!isAuthEndpoint) {
      console.warn('[TokenInterceptor] No token, skipping Authorization:', req.url);
    }
    return next(req);
  }

  if (isAuthEndpoint) {
    console.info('[TokenInterceptor] Skipping for auth endpoint:', req.url);
    return next(req);
  }

  if (tokenUtils.willExpireSoon(token, 60_000) && refreshToken) {
    console.info('[TokenInterceptor] Token expiring, refreshing...');
    return from(authService.refresh({refreshToken, email})).pipe(
      switchMap((res) => {
        localStorage.setItem('access_token', res.authenticationToken);
        localStorage.setItem('refresh_token', res.refreshToken);

        const refreshedReq = req.clone({
          setHeaders: {
            Authorization: `Bearer ${res.authenticationToken}`,
          },
        });

        return next(refreshedReq);
      }),
      catchError((err) => {
        console.error('[TokenInterceptor] Refresh failed:', err);
        return next(req); // fallback to original request
      })
    );
  }

  const cloned = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });

  return next(cloned);
};
