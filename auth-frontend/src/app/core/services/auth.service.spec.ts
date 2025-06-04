import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting,} from '@angular/common/http/testing';

import {AuthService} from './auth.service';
import {environment} from '../../../environments/environment';
import {
  AuthResponse,
  LoginRequest,
  MeResponse,
  RefreshTokenRequest,
  RegisterRequest,
} from '../../shared/models/auth.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  const BASE_URL = `${environment.apiUrl}/api/auth`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should login and return tokens', () => {
    const payload: LoginRequest = {
      email: 'john@example.com',
      password: '123456',
    };

    const mockResponse: AuthResponse = {
      authenticationToken: 'abc',
      refreshToken: 'xyz',
      expiresAt: '2025-06-01T00:00:00Z',
      username: 'john@example.com',
    };

    service.login(payload).subscribe((res) => {
      expect(res.authenticationToken).toBe('abc');
      expect(res.refreshToken).toBe('xyz');
    });

    const req = httpMock.expectOne(`${BASE_URL}/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(mockResponse);
  });

  it('should register a new user and return plain text', () => {
    const payload: RegisterRequest = {
      firstName: 'John',
      lastName: 'Doe',
      email: 'john@example.com',
      password: 'securePass123',
    };

    service.register(payload).subscribe((res) => {
      expect(res).toBe('Registered');
    });

    const req = httpMock.expectOne(`${BASE_URL}/register`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush('Registered');
  });

  it('should refresh the token', () => {
    const payload: RefreshTokenRequest = {
      email: 'john@example.com',
      refreshToken: 'refresh-token',
    };

    const mockResponse: AuthResponse = {
      authenticationToken: 'new-token',
      refreshToken: 'refresh-token',
      expiresAt: '2025-06-01T00:00:00Z',
      username: 'john@example.com',
    };

    service.refresh(payload).subscribe((res) => {
      expect(res.authenticationToken).toBe('new-token');
    });

    const req = httpMock.expectOne(`${BASE_URL}/refresh`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(mockResponse);
  });

  it('should logout and return confirmation', () => {
    const payload: RefreshTokenRequest = {
      email: 'john@example.com',
      refreshToken: 'xyz',
    };

    service.logout(payload).subscribe((res) => {
      expect(res).toBe('Logged out');
    });

    const req = httpMock.expectOne(`${BASE_URL}/logout`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush('Logged out');
  });

  it('should fetch current user info', () => {
    const mockUser: MeResponse = {
      id: 1,
      firstName: 'John',
      lastName: 'Doe',
      email: 'john@example.com',
    };

    service.getCurrentUser().subscribe((res) => {
      expect(res.email).toBe('john@example.com');
      expect(res.firstName).toBe('John');
    });

    const req = httpMock.expectOne(`${BASE_URL}/me`);
    expect(req.request.method).toBe('GET');
    req.flush(mockUser);
  });
});
