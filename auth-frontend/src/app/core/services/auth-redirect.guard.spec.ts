import {runInInjectionContext} from '@angular/core';
import {TestBed} from '@angular/core/testing';

import {authRedirectGuard} from './auth-redirect.guard';
import {
  generateFakeToken,
  mockRoute,
  mockState,
  setupTestBedWithRouter,
} from '../__test-utils__/auth.guard-test-utils';
import {UserStoreService} from '../../features/auth/store/user-store.service';

describe('authRedirectGuard', () => {
  let userStore: UserStoreService;

  beforeEach(() => {
    setupTestBedWithRouter();
    userStore = TestBed.inject(UserStoreService);
    localStorage.clear();
  });

  it('should allow access if no token is present', () => {
    spyOn(userStore, 'isLoggedIn').and.returnValue(false);
    spyOn(localStorage, 'getItem').and.returnValue(null);

    const result = runInInjectionContext(TestBed, () =>
      authRedirectGuard(mockRoute, mockState)
    );

    expect(result).toBeTrue();
  });

  it('should allow access if token exists but user is not logged in', () => {
    const token = generateFakeToken({exp: Date.now() / 1000 + 60});
    spyOn(userStore, 'isLoggedIn').and.returnValue(false);
    spyOn(localStorage, 'getItem').and.returnValue(token);

    const result = runInInjectionContext(TestBed, () =>
      authRedirectGuard(mockRoute, mockState)
    );

    expect(result).toBeTrue();
  });

  it('should redirect to /dashboard if token exists and user is logged in', () => {
    const token = generateFakeToken({exp: Date.now() / 1000 + 60});
    spyOn(userStore, 'isLoggedIn').and.returnValue(true);
    spyOn(localStorage, 'getItem').and.returnValue(token);

    const result = runInInjectionContext(TestBed, () =>
      authRedirectGuard(mockRoute, mockState)
    );

    expect(result.toString().endsWith('/dashboard')).toBeTrue();
  });
});
