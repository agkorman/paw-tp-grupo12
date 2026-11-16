import { StrictMode } from 'react';
import { act, cleanup, render, screen } from '@testing-library/react';
import { afterEach, expect, it, vi } from 'vitest';
import { createHttpClient } from '../../../shared/api/httpClient';
import { createAuthSession } from '../model/authSession';
import { AUTH_RELATIONS } from '../model/auth.contract';
import { AuthProvider, useAuth } from './AuthProvider';
import { AuthNotices } from '../components/AuthNotices';
import '../../../app/i18n';

afterEach(() => { cleanup(); vi.unstubAllGlobals(); });

it('keeps public content visible during restoration and restores only once under StrictMode', async () => {
  const baseUrl = 'http://localhost/webapp/api';
  const userUrl = `${baseUrl}/users/1`, sessionUrl = `${userUrl}/sessions/2`;
  const refreshToken = `rt_${'a'.repeat(43)}`;
  let complete!: (response: Response) => void;
  const response = new Promise<Response>((resolve) => { complete = resolve; });
  const requests: Request[] = [];
  vi.stubGlobal('fetch', (request: Request) => {
    requests.push(request);
    if (request.url === baseUrl) return response;
    return Promise.resolve(new Response(JSON.stringify({ id: '1', displayName: 'Nica', email: 'nica@example.test',
      roles: ['USER'], preferredLocale: 'es', _links: {} }), { headers: { 'Content-Type': 'application/json' } }));
  });
  const session = createAuthSession({ baseUrl,
    storage: { read: () => ({ refreshToken, userUrl, sessionUrl, rememberMe: false }), save: () => {}, clear: () => true },
    createClient: (authentication) => createHttpClient({ baseUrl, authentication }), applyLocale: () => {}, saveLocalLocale: () => {},
  });
  function Consumer() {
    const auth = useAuth();
    return <><p>Public catalog</p><p>{auth.status}</p>
      {auth.status === 'authenticated' && <p>Private account: {auth.user?.displayName}</p>}</>;
  }
  render(<StrictMode><AuthProvider session={session}><Consumer /></AuthProvider></StrictMode>);
  expect(screen.getByText('Public catalog')).toBeInTheDocument();
  expect(screen.getByText('restoring')).toBeInTheDocument();
  expect(screen.queryByText(/Private account/)).not.toBeInTheDocument();

  await act(async () => {
    complete(new Response('{}', { headers: { 'X-Access-Token': 'access', 'X-Refresh-Token': `rt_${'b'.repeat(43)}`,
      Link: `<${userUrl}>; rel="${AUTH_RELATIONS.user}", <${sessionUrl}>; rel="${AUTH_RELATIONS.session}"` } }));
    await session.start();
  });

  expect(screen.getByText('Private account: Nica')).toBeInTheDocument();
  expect(requests.filter((request) => request.url === baseUrl)).toHaveLength(1);
});

it('renders a localized storage failure while retaining public navigation', async () => {
  const session = createAuthSession({ baseUrl: 'http://localhost/api',
    storage: { read: () => undefined, save: () => {}, clear: () => false },
    createClient: (authentication) => createHttpClient({ baseUrl: 'http://localhost/api', authentication }),
    applyLocale: () => {}, saveLocalLocale: () => {},
  });
  render(<AuthProvider session={session}><AuthNotices /><p>Public catalog</p></AuthProvider>);

  await act(async () => { await session.login('nica@example.test', 'password', false).catch(() => {}); });

  expect(screen.getByText(/No pudimos guardar|We could not save/)).toBeInTheDocument();
  expect(screen.getByText('Public catalog')).toBeInTheDocument();
});
