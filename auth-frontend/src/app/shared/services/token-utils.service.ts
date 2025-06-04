import {Injectable} from '@angular/core';
import {jwtDecode} from 'jwt-decode';

interface JwtPayload {
  exp: number;
  sub: string;

  [key: string]: any;
}

@Injectable({providedIn: 'root'})
export class TokenUtilsService {
  /**
   * Returns the expiration time in milliseconds since epoch.
   */
  getTokenExpiration(token: string): number {
    try {
      const decoded: JwtPayload = jwtDecode(token);
      return decoded.exp * 1000;
    } catch {
      return 0;
    }
  }

  /**
   * Checks whether the token is already expired.
   */
  isTokenExpired(token: string): boolean {
    return this.getTokenExpiration(token) < Date.now();
  }

  /**
   * Returns true if token will expire within the given threshold.
   */
  willExpireSoon(token: string, withinMs: number): boolean {
    return this.getTokenExpiration(token) - Date.now() <= withinMs;
  }

  /**
   * Extracts the 'sub' (subject/email) from the token.
   */
  decodeEmailFromJWT(token: string | null): string | null {
    try {
      return token ? jwtDecode<JwtPayload>(token).sub ?? null : null;
    } catch {
      return null;
    }
  }
}
