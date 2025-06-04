import {inject} from '@angular/core';
import {CanActivateFn, Router} from '@angular/router';
import {UserStoreService} from '../../features/auth/store/user-store.service';

export const authRedirectGuard: CanActivateFn = () => {
  const router = inject(Router);
  const userStore = inject(UserStoreService);

  if (userStore.isLoggedIn()) {
    console.info('[authRedirectGuard] 🔁 Already logged in — redirecting to /dashboard');
    return router.createUrlTree(['/dashboard']);
  }

  return true;
};
