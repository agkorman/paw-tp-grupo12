// @vitest-environment node
import { afterEach, describe, expect, it, vi } from 'vitest';
import { createHttpClient } from '../../../shared/api/httpClient';
import { AUTH_RELATIONS } from './auth.contract';
import { createAuthSession } from './authSession';
import { createAuthStorage, type AuthStorage, type StoredSession } from './authStorage';

const baseUrl = 'https://paw.test/webapp/api';
const userUrl = `${baseUrl}/users/1`;
const sessionUrl = `${userUrl}/sessions/10`;
const refresh1 = `rt_${'a'.repeat(43)}`;
const refresh2 = `rt_${'b'.repeat(43)}`;
const refresh3 = `rt_${'c'.repeat(43)}`;
const owner = { id: '1', displayName: 'Nica', email: 'nica@example.test', preferredLocale: 'es',
  roles: ['USER'], _links: { self: { href: userUrl } } };

function json(body: unknown, status = 200, headers: Record<string, string> = {}) {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/vnd.paw.user.v1+json', ...headers } });
}
function issued(refresh = refresh2, access = 'access-2', status = 200) {
  return json({}, status, { 'X-Access-Token': access, 'X-Refresh-Token': refresh,
    Link: `<${userUrl}>; rel="${AUTH_RELATIONS.user}", <${sessionUrl}>; rel="${AUTH_RELATIONS.session}"` });
}
function deferred<T>() {
  let resolve!: (value: T) => void;
  const promise = new Promise<T>((done) => { resolve = done; });
  return { promise, resolve };
}
function setup(saved?: StoredSession, storageOverride?: AuthStorage) {
  let persisted = saved;
  const storage: AuthStorage = storageOverride ?? {
    read: () => persisted, save: (value) => { persisted = { ...value }; }, clear: () => { persisted = undefined; return true; },
  };
  const requests: Request[] = [];
  let route: (request: Request) => Response | Promise<Response> = (request) => {
    if (request.method === 'DELETE') return new Response(null, { status: 204 });
    if (request.url === userUrl) return json(owner);
    return issued();
  };
  vi.stubGlobal('fetch', async (request: Request) => { requests.push(request); return route(request); });
  const locales: string[] = [];
  const savedLocales: string[] = [];
  const auth = createAuthSession({ baseUrl, storage,
    createClient: (authentication) => createHttpClient({ baseUrl, authentication }),
    applyLocale: (locale) => { locales.push(locale); }, saveLocalLocale: (locale) => { savedLocales.push(locale); },
  });
  return { auth, requests, locales, savedLocales, stored: () => persisted,
    route: (handler: typeof route) => { route = handler; } };
}
const saved = (): StoredSession => ({ refreshToken: refresh1, rememberMe: false, userUrl, sessionUrl });

afterEach(() => vi.unstubAllGlobals());

describe('startup and login', () => {
  it('starts anonymous without making a request when there is no stored session', async () => {
    const harness = setup();

    await harness.auth.start();

    expect(harness.auth.getSnapshot()).toEqual({ status: 'anonymous', notices: [] });
    expect(harness.requests).toHaveLength(0);
  });

  it('restores once, rotates through the index, follows the user link and applies the profile language', async () => {
    const harness = setup(saved());

    await Promise.all([harness.auth.start(), harness.auth.start()]);

    expect(harness.requests.map((request) => [request.url, request.headers.get('Authorization')])).toEqual([
      [baseUrl, `Bearer ${refresh1}`], [userUrl, 'Bearer access-2'],
    ]);
    expect(harness.auth.getSnapshot()).toMatchObject({ status: 'authenticated', user: owner });
    expect(harness.stored()?.refreshToken).toBe(refresh2);
    expect(harness.locales).toEqual(['es']);
    expect(JSON.stringify(harness.auth.getSnapshot())).not.toMatch(/rt_|access-2/);
  });

  it('uses UTF-8 Basic email/password and the chosen remember-me header', async () => {
    const harness = setup();
    const email = 'nicá@example.test', password = 'contraseña:123';

    await harness.auth.login(email, password, true);

    const authorization = harness.requests[0].headers.get('Authorization')!;
    expect(new TextDecoder().decode(Uint8Array.from(atob(authorization.slice(6)), (char) => char.charCodeAt(0)))).toBe(`${email}:${password}`);
    expect(harness.requests[0].headers.get('X-Remember-Me')).toBe('true');
    expect(harness.stored()?.rememberMe).toBe(true);
    expect(JSON.stringify(harness.auth.getSnapshot())).not.toContain(password);
  });

  it('returns invalid credentials to the form without publishing an authenticated identity', async () => {
    const harness = setup();
    harness.route(() => json({ code: 'INVALID_CREDENTIALS' }, 401));

    const result = harness.auth.login('nica@example.test', 'incorrect', false);

    await expect(result).rejects.toMatchObject({ status: 401, code: 'INVALID_CREDENTIALS' });
    expect(harness.auth.getSnapshot().status).toBe('anonymous');
    expect(harness.stored()).toBeUndefined();
  });

  it('refuses to finish login when storage fails and revokes the issued session', async () => {
    const storage: AuthStorage = { read: () => undefined, clear: () => true, save: () => { throw new Error('quota'); } };
    const harness = setup(undefined, storage);

    const result = harness.auth.login('nica@example.test', 'password', false);

    await expect(result).rejects.toMatchObject({ code: 'AUTH_STORAGE_UNAVAILABLE' });
    expect(harness.auth.getSnapshot()).toEqual({ status: 'anonymous', notices: ['storageUnavailable'] });
    expect(harness.requests.map((request) => request.method)).toEqual(['GET', 'DELETE']);
    expect(harness.requests[1].url).toBe(sessionUrl);
    expect(harness.requests[1].headers.get('Authorization')).toBe(`Bearer ${refresh2}`);
  });

  it('handles inaccessible storage at startup without an unhandled rejection', async () => {
    const storage = createAuthStorage(baseUrl, { local: () => { throw new Error('denied'); }, session: () => { throw new Error('denied'); } });
    const harness = setup(undefined, storage);

    await harness.auth.start();

    expect(harness.auth.getSnapshot()).toEqual({ status: 'anonymous', notices: ['storageUnavailable', 'storageClearFailed'] });
    expect(harness.requests).toHaveLength(0);
  });

  it('keeps the rotated credentials when the profile cannot load and lets the user retry', async () => {
    const harness = setup(saved());
    let offlineProfile = true;
    harness.route((request) => request.url === userUrl
      ? offlineProfile ? json({}, 503) : json(owner)
      : request.headers.get('Authorization') === `Bearer ${refresh1}` ? issued() : json({}));

    await harness.auth.start();
    expect(harness.auth.getSnapshot()).toEqual({ status: 'unavailable', notices: ['profileUnavailable'] });
    expect(harness.stored()?.refreshToken).toBe(refresh2);
    offlineProfile = false;
    await harness.auth.retryProfile();

    expect(harness.auth.getSnapshot()).toMatchObject({ status: 'authenticated', user: owner });
    expect(harness.requests.filter((request) => request.headers.get('Authorization') === `Bearer ${refresh1}`)).toHaveLength(1);
  });

  it('does not retry a refresh whose response was lost', async () => {
    const harness = setup(saved());
    harness.route(() => { throw new TypeError('network failed'); });

    await harness.auth.start();

    expect(harness.auth.getSnapshot()).toEqual({ status: 'anonymous', notices: ['refreshUncertain'] });
    expect(harness.stored()).toBeUndefined();
    expect(harness.requests).toHaveLength(1);
  });

  it('clears a server-rejected stored session', async () => {
    const harness = setup(saved());
    harness.route(() => json({ code: 'SESSION_REVOKED' }, 401));

    await harness.auth.start();

    expect(harness.auth.getSnapshot()).toEqual({ status: 'anonymous', notices: ['sessionEnded'] });
    expect(harness.stored()).toBeUndefined();
  });
});

describe('renewal within one tab', () => {
  it('spends one refresh for concurrent expired requests and retries waiters with the new access', async () => {
    const harness = setup();
    await harness.auth.login('nica@example.test', 'password', false);
    const releaseRotation = deferred<Response>();
    const rotating = deferred<void>();
    harness.route((request) => {
      if (request.url === userUrl) return json({ ...owner, roles: ['USER', 'ADMIN'] });
      const authorization = request.headers.get('Authorization');
      if (authorization === 'Bearer access-2') return json({ code: 'TOKEN_EXPIRED' }, 401);
      if (authorization === `Bearer ${refresh2}`) { rotating.resolve(); return releaseRotation.promise; }
      return json({ url: request.url });
    });

    const first = harness.auth.client.request({ url: 'cars/1', method: 'PATCH', data: { name: 'Changed' } });
    await rotating.promise;
    const second = harness.auth.client.request({ url: 'cars/2' });
    releaseRotation.resolve(issued(refresh3, 'access-3'));
    await Promise.all([first, second]);

    expect(harness.requests.filter((request) => request.headers.get('Authorization') === `Bearer ${refresh2}`)).toHaveLength(1);
    const request2 = harness.requests.filter((request) => request.url.endsWith('/cars/2'));
    expect(request2.at(-1)?.headers.get('Authorization')).toBe('Bearer access-3');
    expect(harness.stored()?.refreshToken).toBe(refresh3);
    expect(harness.auth.getSnapshot().user?.roles).toEqual(['USER', 'ADMIN']);
  });

  it('saves tokens from a failed mutation and preserves the original error without replaying the mutation again', async () => {
    const harness = setup();
    await harness.auth.login('nica@example.test', 'password', false);
    harness.route((request) => {
      if (request.url === userUrl) return json(owner);
      if (request.headers.get('Authorization') === 'Bearer access-2') return json({ code: 'TOKEN_EXPIRED' }, 401);
      const headers = Object.fromEntries(issued(refresh3, 'access-3').headers.entries());
      return json({ code: 'VALIDATION_ERROR', fieldErrors: [{ field: 'name', code: 'required' }] }, 400, headers);
    });

    const result = harness.auth.client.request({ url: 'cars/1', method: 'PATCH', data: {} });

    await expect(result).rejects.toMatchObject({ code: 'VALIDATION_ERROR', fieldErrors: [{ field: 'name', code: 'required' }] });
    expect(harness.stored()?.refreshToken).toBe(refresh3);
    expect(harness.auth.getSnapshot().status).toBe('authenticated');
    expect(harness.requests.filter((request) => request.method === 'PATCH')).toHaveLength(2);
  });

  it('ends the local session when an in-flight refresh loses its response, without replaying a mutation', async () => {
    const harness = setup();
    await harness.auth.login('nica@example.test', 'password', false);
    harness.route((request) => {
      if (request.headers.get('Authorization') === 'Bearer access-2') return json({ code: 'TOKEN_EXPIRED' }, 401);
      throw new TypeError('fetch failed');
    });

    const result = harness.auth.client.request({ url: 'cars', method: 'POST', data: {} });

    await expect(result).rejects.toMatchObject({ kind: 'network' });
    expect(harness.auth.getSnapshot()).toEqual({ status: 'anonymous', notices: ['refreshUncertain'] });
    expect(harness.stored()).toBeUndefined();
    expect(harness.requests.filter((request) => request.method === 'POST')).toHaveLength(2);
  });

  it.each([403, 500])('keeps the session after a regular access request returns %s', async (status) => {
    const harness = setup();
    await harness.auth.login('nica@example.test', 'password', false);
    harness.route(() => json({ code: 'ERROR' }, status));

    const result = harness.auth.client.request({ url: 'cars' });

    await expect(result).rejects.toMatchObject({ status });
    expect(harness.auth.getSnapshot().status).toBe('authenticated');
    expect(harness.stored()?.refreshToken).toBe(refresh2);
  });

  it('keeps the session after a regular access network failure', async () => {
    const harness = setup();
    await harness.auth.login('nica@example.test', 'password', false);
    harness.route(() => { throw new TypeError('fetch failed'); });

    const result = harness.auth.client.request({ url: 'cars' });

    await expect(result).rejects.toMatchObject({ kind: 'network' });
    expect(harness.auth.getSnapshot().status).toBe('authenticated');
  });
});

describe('logout and stale responses', () => {
  it('clears identity immediately and reports an unconfirmed remote logout', async () => {
    const harness = setup();
    await harness.auth.login('nica@example.test', 'password', false);
    harness.route(() => { throw new TypeError('offline'); });

    const result = harness.auth.logout();
    expect(harness.auth.getSnapshot().user).toBeUndefined();
    expect(harness.stored()).toBeUndefined();
    await result;

    expect(harness.auth.getSnapshot()).toEqual({ status: 'anonymous', notices: ['logoutUnconfirmed'] });
    const request = harness.requests.at(-1)!;
    expect([request.method, request.url, request.headers.get('Authorization')]).toEqual(['DELETE', sessionUrl, `Bearer ${refresh2}`]);
  });

  it('discards a profile response arriving after logout', async () => {
    const harness = setup();
    const profile = deferred<Response>(), requested = deferred<void>();
    harness.route((request) => {
      if (request.method === 'DELETE') return new Response(null, { status: 204 });
      if (request.url === userUrl) { requested.resolve(); return profile.promise; }
      return issued();
    });
    const login = harness.auth.login('nica@example.test', 'password', false).catch((error: unknown) => error);
    await requested.promise;

    await harness.auth.logout();
    profile.resolve(json(owner));

    expect(await login).toMatchObject({ kind: 'canceled' });
    expect(harness.auth.getSnapshot()).toEqual({ status: 'anonymous', notices: [] });
    expect(harness.stored()).toBeUndefined();
  });

  it('discards and revokes tokens from a login arriving after logout', async () => {
    const harness = setup();
    const response = deferred<Response>(), requested = deferred<void>();
    harness.route((request) => {
      if (request.method === 'DELETE') return new Response(null, { status: 204 });
      requested.resolve(); return response.promise;
    });
    const login = harness.auth.login('nica@example.test', 'password', false).catch((error: unknown) => error);
    await requested.promise;

    await harness.auth.logout();
    response.resolve(issued());

    expect(await login).toMatchObject({ kind: 'canceled' });
    expect(harness.auth.getSnapshot().status).toBe('anonymous');
    expect(harness.stored()).toBeUndefined();
    expect(harness.requests.at(-1)?.method).toBe('DELETE');
  });

  it('updates the profile language with PATCH before applying the explicit choice', async () => {
    const harness = setup();
    await harness.auth.login('nica@example.test', 'password', false);
    harness.route(() => json({ ...owner, preferredLocale: 'en' }));

    await harness.auth.changeLocale('en');

    const request = harness.requests.at(-1)!;
    expect(request.method).toBe('PATCH');
    expect(await request.json()).toEqual({ preferredLocale: 'en' });
    expect(harness.auth.getSnapshot().user?.preferredLocale).toBe('en');
    expect(harness.locales).toEqual(['es', 'en']);
    expect(harness.savedLocales).toEqual(['en']);
  });
});
