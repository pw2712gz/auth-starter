import {inject} from '@angular/core';
import {CanActivateFn, Router} from '@angular/router';
import {UserStoreService} from '../../features/auth/store/user-store.service';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const userStore = inject(UserStoreService);

  const isLoggedIn = userStore.isLoggedIn();

  if (isLoggedIn) {
    return true;
  }

  console.warn('[authGuard] ❌ Access denied — redirecting to /login');
  return router.createUrlTree(['/login']);
};
