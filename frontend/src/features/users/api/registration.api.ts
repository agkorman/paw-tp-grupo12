import { ApiClientError } from '../../../shared/api/api-error.types';
import { getApiBaseUrl } from '../../../shared/api/apiUrl';
import type { HttpClient } from '../../../shared/api/httpClient';
import { INDEX_MEDIA_TYPE, type ApiIndex } from '../../../shared/api/resource.types';
import { USER_MEDIA_TYPE } from '../model/user.types';

export async function registerUser(client: HttpClient, input: { displayName: string; email: string; password: string }) {
  const index = await client.request<ApiIndex>({ url: getApiBaseUrl(), authentication: 'none', headers: { Accept: INDEX_MEDIA_TYPE } });
  const users = index.data?._links?.users?.href;
  if (!users) throw new ApiClientError('invalid-response', 'Missing users collection link');
  try {
    const result = await client.request({ url: users, method: 'POST', authentication: 'none',
      headers: { Accept: USER_MEDIA_TYPE, 'Content-Type': USER_MEDIA_TYPE },
      data: { displayName: input.displayName, email: input.email, password: input.password } });
    if (result.status !== 201) throw new ApiClientError('invalid-response', 'Unconfirmed registration', { code: 'REGISTRATION_UNCONFIRMED' });
  } catch (error) {
    // A 201 confirms creation even if its optional representation could not be decoded.
    if (error instanceof ApiClientError && error.status === 201) return;
    if (error instanceof ApiClientError && (!error.status && ['network', 'timeout', 'canceled'].includes(error.kind)
        || (error.status ?? 0) >= 500)) {
      throw new ApiClientError(error.kind, 'Registration outcome could not be confirmed', { status: error.status, code: 'REGISTRATION_UNCONFIRMED' });
    }
    throw error;
  }
}
