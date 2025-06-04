import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {of, throwError} from 'rxjs';
import {Router} from '@angular/router';

import {AuthService} from './auth.service';
import {UserSessionService} from './user-session.service';
import {UserStoreService} from '../../features/auth/store/user-store.service';
import {TokenUtilsService} from '../../shared/services/token-utils.service';
import {MeResponse} from '../../shared/models/auth.model';

describe('UserSessionService', () => {
  let service: UserSessionService;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let userStoreSpy: jasmine.SpyObj<UserStoreService>;
  let tokenUtilsSpy: jasmine.SpyObj<TokenUtilsService>;

  const mockUser: MeResponse = {
    id: 1,
    firstName: 'John',
    lastName: 'Doe',
    email: 'john@example.com',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: AuthService,
          useValue: jasmine.createSpyObj('AuthService', ['getCurrentUser', 'logout']),
        },
        {
          provide: UserStoreService,
          useValue: jasmine.createSpyObj('UserStoreService', ['logout'], {
            currentUser: {set: jasmine.createSpy('set')},
          }),
        },
        {
          provide: TokenUtilsService,
          useValue: jasmine.createSpyObj('TokenUtilsService', ['isTokenExpired']),
        },
        {
          provide: Router,
          useValue: jasmine.createSpyObj('Router', ['navigateByUrl']),
        },
        UserSessionService,
      ],
    });

    service = TestBed.inject(UserSessionService);
    authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    userStoreSpy = TestBed.inject(UserStoreService) as jasmine.SpyObj<UserStoreService>;
    tokenUtilsSpy = TestBed.inject(TokenUtilsService) as jasmine.SpyObj<TokenUtilsService>;
  });

  describe('initUserSession', () => {
    it('should fetch and set user if token is valid', () => {
      spyOn(localStorage, 'getItem').and.returnValue('valid-token');
      tokenUtilsSpy.isTokenExpired.and.returnValue(false);
      authServiceSpy.getCurrentUser.and.returnValue(of(mockUser));

      service.initUserSession();

      expect(authServiceSpy.getCurrentUser).toHaveBeenCalled();
      expect(userStoreSpy.currentUser.set).toHaveBeenCalledWith(mockUser);
    });

    it('should log out if token is expired', () => {
      spyOn(localStorage, 'getItem').and.returnValue('expired-token');
      tokenUtilsSpy.isTokenExpired.and.returnValue(true);

      service.initUserSession();

      expect(userStoreSpy.logout).toHaveBeenCalled();
    });

    it('should log out if fetching user fails', () => {
      spyOn(localStorage, 'getItem').and.returnValue('valid-token');
      tokenUtilsSpy.isTokenExpired.and.returnValue(false);
      authServiceSpy.getCurrentUser.and.returnValue(throwError(() => new Error('fail')));

      service.initUserSession();

      expect(userStoreSpy.logout).toHaveBeenCalled();
      expect(userStoreSpy.currentUser.set).not.toHaveBeenCalled();
    });

    it('should log out if no token found in localStorage', () => {
      spyOn(localStorage, 'getItem').and.returnValue(null);

      service.initUserSession();

      expect(userStoreSpy.logout).toHaveBeenCalled();
    });
  });
});
