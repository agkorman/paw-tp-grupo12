import type { ApiLink } from '../../../shared/api/resource.types';

export const USER_MEDIA_TYPE = 'application/vnd.paw.user.v1+json';

/** One canonical user resource; private fields are omitted for other viewers. */
export interface User {
  readonly id: string;
  readonly displayName: string;
  readonly email?: string;
  readonly preferredLocale?: string;
  /** Effective authenticated roles, without the ROLE_ prefix; owner-only. */
  readonly roles?: readonly string[];
  readonly _links: Readonly<Record<string, ApiLink>>;
}
