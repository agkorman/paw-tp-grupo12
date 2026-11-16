import type { ApiLink } from '../../../shared/api/resource.types';

/** Tokens appear only on initial authentication and refresh rotation, never in JSON bodies. */
export const AUTH_HEADERS = {
  accessToken: 'X-Access-Token',
  refreshToken: 'X-Refresh-Token',
  rememberMe: 'X-Remember-Me',
} as const;

/** Extension relations in the HTTP Link header; values are identifiers, not request URLs. */
export const AUTH_RELATIONS = {
  user: 'urn:paw:relation:authenticated-user',
  session: 'urn:paw:relation:authenticated-session',
} as const;

export const AUTH_ERROR_CODES = {
  authenticationRequired: 'AUTHENTICATION_REQUIRED',
  invalidCredentials: 'INVALID_CREDENTIALS',
  tokenExpired: 'TOKEN_EXPIRED',
  invalidToken: 'INVALID_TOKEN',
  sessionExpired: 'SESSION_EXPIRED',
  sessionRevoked: 'SESSION_REVOKED',
  refreshTokenReused: 'REFRESH_TOKEN_REUSED',
  accessDenied: 'ACCESS_DENIED',
} as const;

export const SESSION_MEDIA_TYPE = 'application/vnd.paw.session.v1+json';

/** Private resource; expiresAt is ISO 8601 UTC and never extended by rotation. */
export interface Session {
  readonly id: string;
  readonly expiresAt: string;
  readonly _links: Readonly<Record<string, ApiLink>>;
}
