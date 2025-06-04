import {runInInjectionContext} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {Router} from '@angular/router';
import {authGuard} from './auth.guard';
import {UserStoreService} from '../../features/auth/store/user-store.service';

describe('authGuard', () => {
  let userStore: jasmine.SpyObj<UserStoreService>;
  let router: jasmine.SpyObj<Router>;

  const setLocalToken = (value: string | null) => {
    spyOn(localStorage, 'getItem').and.returnValue(value);
  };

  const runGuard = () =>
    runInInjectionContext(TestBed, () => authGuard(null as any, null as any));

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {
          provide: UserStoreService,
          useValue: jasmine.createSpyObj('UserStoreService', ['isLoggedIn']),
        },
        {
          provide: Router,
          useValue: jasmine.createSpyObj('Router', ['createUrlTree']),
        },
      ],
    });

    userStore = TestBed.inject(UserStoreService) as jasmine.SpyObj<UserStoreService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  it('should allow access if token exists and user is logged in', () => {
    setLocalToken('valid-token');
    userStore.isLoggedIn.and.returnValue(true);

    expect(runGuard()).toBeTrue();
  });

  it('should redirect to /login if token is missing', () => {
    setLocalToken(null);
    userStore.isLoggedIn.and.returnValue(false);
    router.createUrlTree.and.returnValue('/login' as any);

    expect(runGuard()).toBe('/login' as any);
    expect(router.createUrlTree).toHaveBeenCalledWith(['/login']);
  });

  it('should redirect to /login if token exists but user is not logged in', () => {
    setLocalToken('some-token');
    userStore.isLoggedIn.and.returnValue(false);
    router.createUrlTree.and.returnValue('/login' as any);

    expect(runGuard()).toBe('/login' as any);
    expect(router.createUrlTree).toHaveBeenCalledWith(['/login']);
  });
});
