import { afterEach, expect, it } from 'vitest';
import { createAuthStorage } from './authStorage';

const baseUrl = 'https://paw.test/webapp/api';
const key = `paw.auth:${baseUrl}`;
const session = { refreshToken: `rt_${'a'.repeat(43)}`, userUrl: `${baseUrl}/users/1`,
  sessionUrl: `${baseUrl}/users/1/sessions/2`, rememberMe: false };
const storage = () => createAuthStorage(baseUrl, { local: () => localStorage, session: () => sessionStorage });

afterEach(() => { localStorage.clear(); sessionStorage.clear(); });

it('persists only the refresh and resource links in the chosen store, removing the other copy', () => {
  const authStorage = storage();
  localStorage.setItem(key, 'old local value');
  const data = { ...session, accessToken: 'secret-access', password: 'secret-password', email: 'private-email' };

  authStorage.save(data);

  expect(localStorage.getItem(key)).toBeNull();
  expect(authStorage.read()).toEqual(session);
  expect(sessionStorage.getItem(key)).not.toMatch(/secret|private-email/);
});

it('moves the refresh to localStorage when remember-me is selected', () => {
  const authStorage = storage();
  authStorage.save(session);

  authStorage.save({ ...session, rememberMe: true });

  expect(sessionStorage.getItem(key)).toBeNull();
  expect(authStorage.read()).toEqual({ ...session, rememberMe: true });
});

it.each(['invalid JSON', JSON.stringify({ version: 1, ...session, sessionUrl: 'https://other.test/session' }),
  JSON.stringify({ version: 1, ...session, refreshToken: 'not-a-refresh' })])('rejects invalid saved credentials: %s', (raw) => {
  sessionStorage.setItem(key, raw);

  expect(() => storage().read()).toThrow('Authentication storage unavailable');
});

it('does not guess which session to use if both stores contain a copy', () => {
  const raw = JSON.stringify({ version: 1, ...session });
  localStorage.setItem(key, raw);
  sessionStorage.setItem(key, raw);

  expect(() => storage().read()).toThrow('Authentication storage unavailable');
});

it('scopes storage to the deployed API, preserving another app under the same origin', () => {
  localStorage.setItem('paw.auth:https://paw.test/other/api', 'other session');
  const authStorage = storage();
  authStorage.save(session);

  const cleared = authStorage.clear();

  expect(cleared).toBe(true);
  expect(authStorage.read()).toBeUndefined();
  expect(localStorage.getItem('paw.auth:https://paw.test/other/api')).toBe('other session');
});

it('attempts clearing the second store even if accessing the first one throws', () => {
  sessionStorage.setItem(key, 'stale session');
  const authStorage = createAuthStorage(baseUrl, { local: () => { throw new Error('denied'); }, session: () => sessionStorage });

  const cleared = authStorage.clear();

  expect(cleared).toBe(false);
  expect(sessionStorage.getItem(key)).toBeNull();
});
