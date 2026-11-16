// @vitest-environment node
import { afterEach, describe, expect, it, vi } from 'vitest';
import { ApiClientError } from './api-error.types';
import { createHttpClient, DEFAULT_TIMEOUT_MS, type HttpAuthentication, type HttpResponseMetadata } from './httpClient';

const baseUrl = 'https://paw.test/webapp/api';
const userType = 'application/vnd.paw.user.v1+json';
const errorType = 'application/vnd.paw.error.v1+json';
const access = 'Bearer access';

function json(body: unknown, status = 200, headers: Record<string, string> = {}) {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': userType, ...headers } });
}

function mockFetch(handler: (request: Request) => Response | Promise<Response>) {
  const requests: Request[] = [];
  vi.stubGlobal('fetch', async (request: Request) => {
    requests.push(request);
    return handler(request);
  });
  return requests;
}

afterEach(() => {
  vi.unstubAllGlobals();
  vi.useRealTimers();
});

describe('HTTP transport', () => {
  it('preserves canonical URLs, queries, negotiated types, status and headers without cookies or redirects', async () => {
    const requests = mockFetch(() => json({ id: '9007199254740993' }, 201, {
      Location: `${baseUrl}/users/9007199254740993`, Link: '</webapp/api/users/3>; rel="next"', ETag: '"user-1"',
    }));
    const client = createHttpClient({ baseUrl, authentication: { getAuthorization: () => access } });

    const response = await client.request<{ id: string }>({ url: '/webapp/api/users', method: 'POST',
      params: { source: 'a b' }, headers: { Accept: userType, 'Content-Type': userType }, data: { displayName: 'Nica' } });

    expect(response).toMatchObject({ status: 201, data: { id: '9007199254740993' },
      headers: { location: `${baseUrl}/users/9007199254740993`, link: '</webapp/api/users/3>; rel="next"', etag: '"user-1"' } });
    expect(response.url).toBe(`${baseUrl}/users?source=a+b`);
    expect(requests[0].url).toBe(response.url);
    expect(requests[0].credentials).toBe('omit');
    expect(requests[0].redirect).toBe('error');
    expect(requests[0].headers.get('Authorization')).toBe(access);
    expect(requests[0].headers.get('Accept')).toBe(userType);
    expect(requests[0].headers.get('Content-Type')).toBe(userType);
    expect(await requests[0].json()).toEqual({ displayName: 'Nica' });
  });

  it.each([`${baseUrl}/users/1`, '/webapp/api/users/1', 'users/1', '?page=2'])(
    'follows an API link as supplied: %s', async (url) => {
      const requests = mockFetch(() => json({}));
      const client = createHttpClient({ baseUrl });

      await client.request({ url });

      expect(requests[0].url).toBe(new URL(url, `${baseUrl}/`).href);
    },
  );

  it.each(['https://other.test/webapp/api', '//other.test/webapp/api', '/webapp/api-evil',
    '/webapp/login', '../login', '/webapp/api/../../login', '/webapp/api/%2e%2e/login',
    '/webapp/api/users%2f..%2f..%2flogin', '/webapp/api/%252e%252e/login',
    'https://name:password@paw.test/webapp/api', 'data:text/plain,hello'])(
    'rejects unsafe links before reading credentials or making a request: %s', async (url) => {
      const requests = mockFetch(() => json({}));
      let credentialRead = false;
      const client = createHttpClient({ baseUrl, authentication: { getAuthorization: () => {
        credentialRead = true;
        return access;
      } } });

      const failure = await client.request({ url }).catch((error: unknown) => error);

      expect(failure).toBeInstanceOf(ApiClientError);
      expect(failure).toMatchObject({ kind: 'configuration' });
      expect(requests).toHaveLength(0);
      expect(credentialRead).toBe(false);
    },
  );

  it('preserves application validation errors and saves issued tokens before rejecting', async () => {
    const body = { code: 'VALIDATION_ERROR', message: 'Request validation failed',
      fieldErrors: [{ field: 'displayName', code: 'Size', params: { max: 80 } }] };
    mockFetch(() => json(body, 400, { 'X-Access-Token': 'new-access', 'X-Refresh-Token': 'new-refresh',
      'Retry-After': '5', 'WWW-Authenticate': 'Bearer', Link: '</webapp/api/users/1>; rel="self"' }));
    const observed: HttpResponseMetadata[] = [];
    const client = createHttpClient({ baseUrl, authentication: {
      getAuthorization: () => access, onResponse: (response) => { observed.push(response); },
    } });

    const failure = await client.request({ url: 'users/1' }).catch((error: unknown) => error);

    expect(observed[0].headers['x-refresh-token']).toBe('new-refresh');
    expect(failure).toMatchObject({ kind: 'http', status: 400, code: 'VALIDATION_ERROR',
      fieldErrors: body.fieldErrors, body, headers: { 'retry-after': '5', 'www-authenticate': 'Bearer' } });
    expect(JSON.stringify(failure)).not.toMatch(/new-access|new-refresh|Bearer access/);
    expect(failure).not.toHaveProperty('config');
  });

  it('retains a non-JSON gateway failure as an HTTP error', async () => {
    mockFetch(() => new Response('<h1>Unavailable</h1>', { status: 502, headers: { 'Retry-After': '10' } }));
    const client = createHttpClient({ baseUrl });

    const failure = await client.request({ url: baseUrl }).catch((error: unknown) => error);

    expect(failure).toMatchObject({ kind: 'http', status: 502, body: '<h1>Unavailable</h1>', headers: { 'retry-after': '10' } });
  });

  it('reports malformed successful JSON after observing token headers', async () => {
    mockFetch(() => new Response('{broken', { headers: { 'X-Refresh-Token': 'rotated' } }));
    let refresh: string | undefined;
    const client = createHttpClient({ baseUrl, authentication: { getAuthorization: () => access,
      onResponse: ({ headers }) => { refresh = headers['x-refresh-token']; } } });

    const failure = await client.request({ url: baseUrl }).catch((error: unknown) => error);

    expect(failure).toMatchObject({ kind: 'invalid-response', status: 200 });
    expect(refresh).toBe('rotated');
  });

  it.each([204, 304])('exposes an empty %s response and its headers', async (status) => {
    const requests = mockFetch(() => new Response(null, { status, headers: { ETag: '"1"' } }));
    const client = createHttpClient({ baseUrl });

    const response = await client.request({ url: 'users/1', method: 'DELETE' });

    expect(response).toMatchObject({ data: undefined, status, headers: { etag: '"1"' } });
    expect(requests[0].headers.has('Content-Type')).toBe(false);
  });

  it('lets the browser supply the multipart boundary', async () => {
    const requests = mockFetch(() => json({}));
    const client = createHttpClient({ baseUrl });
    const data = new FormData();
    data.append('image', new Blob(['image bytes'], { type: 'image/png' }), 'car.png');

    await client.request({ url: 'images', method: 'POST', data, headers: { 'Content-Type': userType } });

    expect(requests[0].headers.get('Content-Type')).toMatch(/^multipart\/form-data; boundary=/);
    expect((await requests[0].formData()).get('image')).toBeInstanceOf(Blob);
  });

  it('supports binary success responses and JSON errors for the same request', async () => {
    let next = new Response(new Uint8Array([1, 2, 3]), { headers: { 'Content-Type': 'image/png' } });
    mockFetch(() => next);
    const client = createHttpClient({ baseUrl });

    const response = await client.request<Blob>({ url: 'images/1', responseType: 'blob' });
    next = json({ code: 'NOT_FOUND' }, 404, { 'Content-Type': errorType });
    const failure = await client.request({ url: 'images/1', responseType: 'blob' }).catch((error: unknown) => error);

    expect([...new Uint8Array(await response.data.arrayBuffer())]).toEqual([1, 2, 3]);
    expect(failure).toMatchObject({ kind: 'http', status: 404, code: 'NOT_FOUND' });
  });

  it('uses explicit credentials only in manual mode and omits automatic credentials in none mode', async () => {
    const requests = mockFetch(() => json({}));
    const client = createHttpClient({ baseUrl, authentication: { getAuthorization: () => access } });

    await client.request({ url: baseUrl, authentication: 'manual', headers: { Authorization: 'Basic dTpw' } });
    await client.request({ url: baseUrl, authentication: 'none' });
    const failure = await client.request({ url: baseUrl, headers: { Authorization: 'Basic dTpw' } }).catch((error: unknown) => error);

    expect(requests.map((request) => request.headers.get('Authorization'))).toEqual(['Basic dTpw', null]);
    expect(failure).toMatchObject({ kind: 'configuration' });
  });
});

describe('bounded auth recovery', () => {
  it('repeats the original mutation once with the credential supplied by the coordinator', async () => {
    const requests = mockFetch((request) => request.headers.get('Authorization') === access
      ? json({ code: 'TOKEN_EXPIRED' }, 401) : json({ id: '1', displayName: 'Nica' }));
    const client = createHttpClient({ baseUrl, authentication: { getAuthorization: () => access,
      recoverExpiredToken: ({ retry }) => retry('Bearer rt_rotated') } });

    const response = await client.request({ url: 'users/1', method: 'PATCH', headers: { 'Content-Type': userType }, data: { displayName: 'Nica' } });

    expect(response.data).toEqual({ id: '1', displayName: 'Nica' });
    expect(requests).toHaveLength(2);
    expect(requests.map((request) => request.method)).toEqual(['PATCH', 'PATCH']);
    expect(await requests[0].text()).toBe(await requests[1].text());
  });

  it('does not recursively retry if the replacement also receives TOKEN_EXPIRED', async () => {
    const requests = mockFetch(() => json({ code: 'TOKEN_EXPIRED' }, 401));
    const client = createHttpClient({ baseUrl, authentication: { getAuthorization: () => access,
      recoverExpiredToken: ({ retry }) => retry('Bearer replacement') } });

    const failure = await client.request({ url: baseUrl }).catch((error: unknown) => error);

    expect(failure).toMatchObject({ kind: 'http', code: 'TOKEN_EXPIRED' });
    expect(requests).toHaveLength(2);
  });

  it('rejects a second invocation of the retry callback', async () => {
    const requests = mockFetch((request) => request.headers.get('Authorization') === access
      ? json({ code: 'TOKEN_EXPIRED' }, 401) : json({}));
    const client = createHttpClient({ baseUrl, authentication: { getAuthorization: () => access,
      recoverExpiredToken: async ({ retry }) => { await retry('Bearer next'); return retry('Bearer next'); } } });

    const failure = await client.request({ url: baseUrl }).catch((error: unknown) => error);

    expect(failure).toMatchObject({ kind: 'configuration' });
    expect(requests).toHaveLength(2);
  });

  it.each([[401, 'INVALID_TOKEN'], [403, 'ACCESS_DENIED'], [500, 'INTERNAL_ERROR']])(
    'does not retry %s %s', async (status, code) => {
      const requests = mockFetch(() => json({ code }, status as number));
      const client = createHttpClient({ baseUrl, authentication: { getAuthorization: () => access,
        recoverExpiredToken: ({ retry }) => retry('Bearer refresh') } });

      const failure = await client.request({ url: 'users/1', method: 'PATCH', data: {} }).catch((error: unknown) => error);

      expect(failure).toMatchObject({ kind: 'http', status, code });
      expect(requests).toHaveLength(1);
    },
  );

  it('does not recover manual authentication requests', async () => {
    const requests = mockFetch(() => json({ code: 'TOKEN_EXPIRED' }, 401));
    const client = createHttpClient({ baseUrl, authentication: { getAuthorization: () => access,
      recoverExpiredToken: ({ retry }) => retry('Bearer refresh') } });

    const failure = await client.request({ url: baseUrl, authentication: 'manual', headers: { Authorization: 'Bearer explicit' } }).catch((error: unknown) => error);

    expect(failure).toMatchObject({ status: 401 });
    expect(requests).toHaveLength(1);
  });
});

describe('transport failures', () => {
  const authentication: HttpAuthentication = { getAuthorization: () => access,
    recoverExpiredToken: ({ retry }) => retry('Bearer refresh') };

  function pendingFetch() {
    return mockFetch((request) => new Promise<Response>((_resolve, reject) => {
      request.signal.addEventListener('abort', () => reject(request.signal.reason), { once: true });
    }));
  }

  it('distinguishes a network failure without retrying or exposing Axios credentials', async () => {
    const requests = mockFetch(() => { throw new TypeError('fetch failed'); });
    const client = createHttpClient({ baseUrl, authentication });

    const failure = await client.request({ url: baseUrl, method: 'POST', data: { password: 'secret' } }).catch((error: unknown) => error);

    expect(failure).toMatchObject({ kind: 'network', status: undefined });
    expect(JSON.stringify(failure)).not.toMatch(/secret|Bearer/);
    expect(requests).toHaveLength(1);
  });

  it.each([undefined, 60_000])('times out at the configured deadline (%s)', async (timeoutMs) => {
    vi.useFakeTimers();
    const requests = pendingFetch();
    const client = createHttpClient({ baseUrl, authentication });
    let finished = false;
    const result = client.request({ url: baseUrl, timeoutMs }).catch((error: unknown) => { finished = true; return error; });

    await vi.advanceTimersByTimeAsync((timeoutMs ?? DEFAULT_TIMEOUT_MS) - 1);
    expect(finished).toBe(false);
    await vi.advanceTimersByTimeAsync(1);

    expect(await result).toMatchObject({ kind: 'timeout', status: undefined });
    expect(requests).toHaveLength(1);
  });

  it('distinguishes cancellation from a timeout and never retries', async () => {
    const requests = pendingFetch();
    const controller = new AbortController();
    const client = createHttpClient({ baseUrl, authentication });
    const result = client.request({ url: baseUrl, signal: controller.signal }).catch((error: unknown) => error);

    controller.abort();

    expect(await result).toMatchObject({ kind: 'canceled' });
    expect(requests).toHaveLength(1);
  });
});
