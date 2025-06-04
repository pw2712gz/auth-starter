/**
 * Request body for login API
 */
export interface LoginRequest {
  email: string;
  password: string;
}

/**
 * Request body for registration API
 */
export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

/**
 * Request body for token refresh API
 */
export interface RefreshTokenRequest {
  email: string;
  refreshToken: string;
}

/**
 * Response from login or refresh endpoint
 */
export interface AuthResponse {
  authenticationToken: string;
  refreshToken: string;
  expiresAt: string;
  username: string;
}

/**
 * Response from `/me` endpoint to get current user details
 */
export interface MeResponse {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
}
