import { ApiClientError, isRecord } from '../../../shared/api/api-error.types';
import type { HttpClient } from '../../../shared/api/httpClient';
import { USER_MEDIA_TYPE, type User } from '../model/user.types';

export interface OwnerUser extends User {
  readonly email: string;
  readonly preferredLocale: 'es' | 'en';
  readonly roles: readonly string[];
}

function owner(data: unknown): OwnerUser {
  if (!isRecord(data) || typeof data.id !== 'string' || typeof data.displayName !== 'string'
      || typeof data.email !== 'string' || !['es', 'en'].includes(String(data.preferredLocale))
      || !Array.isArray(data.roles) || !data.roles.every((role) => typeof role === 'string')
      || !isRecord(data._links)) throw new ApiClientError('invalid-response', 'Invalid owner user representation');
  return data as unknown as OwnerUser;
}

export async function readOwner(client: HttpClient, url: string, authorization: string): Promise<OwnerUser> {
  const response = await client.request<unknown>({ url, authentication: 'manual',
    headers: { Accept: USER_MEDIA_TYPE, Authorization: authorization } });
  return owner(response.data);
}

export async function updateOwnerLocale(client: HttpClient, url: string, preferredLocale: 'es' | 'en'): Promise<OwnerUser> {
  const response = await client.request<unknown>({ url, method: 'PATCH',
    headers: { Accept: USER_MEDIA_TYPE, 'Content-Type': USER_MEDIA_TYPE }, data: { preferredLocale } });
  return owner(response.data);
}
