import { act, cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom';
import { afterEach, expect, it, vi } from 'vitest';
import { createHttpClient } from '../../../shared/api/httpClient';
import { HttpClientProvider } from '../../../shared/api/HttpClientProvider';
import { createAuthSession } from '../model/authSession';
import { AuthProvider } from '../providers/AuthProvider';
import { AuthNotices } from '../components/AuthNotices';
import { AUTH_RELATIONS } from '../model/auth.contract';
import { LoginPage } from './LoginPage';
import { RegisterPage } from './RegisterPage';
import i18n from '../../../app/i18n';

const base = `${window.location.origin}/api`;
const user = { id: '1', displayName: 'nica', email: 'nica@example.test', preferredLocale: 'es', roles: ['USER'], _links: {} };
const json = (body: unknown, status = 200, headers = {}) => new Response(JSON.stringify(body), {
  status, headers: { 'Content-Type': 'application/json', ...headers },
});

function setup(path: string, override?: (request: Request) => Response | Promise<Response> | undefined) {
  const meta = document.createElement('meta'); meta.name = 'paw-api-base-path'; meta.content = '/api'; document.head.appendChild(meta);
  const requests: Request[] = [];
  vi.stubGlobal('fetch', (request: Request) => {
    requests.push(request);
    const custom = override?.(request);
    if (custom) return Promise.resolve(custom);
    if (request.method === 'POST') return Promise.resolve(json({ id: '1' }, 201));
    if (request.url.endsWith('/users/1')) return Promise.resolve(json(user));
    if (request.headers.get('Authorization')?.startsWith('Basic ')) return Promise.resolve(json({}, 200, {
      'X-Access-Token': 'access', 'X-Refresh-Token': `rt_${'a'.repeat(43)}`,
      Link: `</api/users/1>; rel="${AUTH_RELATIONS.user}", </api/users/1/sessions/2>; rel="${AUTH_RELATIONS.session}"`,
    }));
    return Promise.resolve(json({ _links: { users: { href: '/api/users' } } }));
  });
  const session = createAuthSession({ baseUrl: base,
    storage: { read: () => undefined, save: () => {}, clear: () => true },
    createClient: (authentication) => createHttpClient({ baseUrl: base, authentication }),
    applyLocale: () => {}, saveLocalLocale: () => {},
  });
  function Destination() { const location = useLocation(); return <p>Destination: {location.pathname}{location.search}{location.hash}</p>; }
  render(<AuthProvider session={session}><HttpClientProvider client={session.client}>
    <MemoryRouter initialEntries={[path]}><AuthNotices /><Routes>
      <Route path="/login" element={<LoginPage />} /><Route path="/register" element={<RegisterPage />} />
      <Route path="/cars" element={<Destination />} /><Route path="/" element={<Destination />} />
    </Routes></MemoryRouter>
  </HttpClientProvider></AuthProvider>);
  return requests;
}

function input(label: string, value: string) { fireEvent.change(screen.getByLabelText(label, { exact: true }), { target: { value } }); }
function fillRegistration() {
  input('Nombre de usuario', 'nica'); input('Email', 'nica@example.test'); input('Contraseña', 'password123'); input('Confirmar contraseña', 'password123');
}
afterEach(() => { cleanup(); vi.unstubAllGlobals(); document.querySelector('meta[name="paw-api-base-path"]')?.remove(); });

it('validates after leaving a field and updates its error while editing', async () => {
  await i18n.changeLanguage('es');
  const requests = setup('/login');
  input('Email', 'incorrect');
  expect(screen.queryByText(/Ingresá un email válido/)).not.toBeInTheDocument();
  fireEvent.blur(screen.getByLabelText('Email'));
  expect(await screen.findByText(/Ingresá un email válido/)).toBeInTheDocument();
  input('Email', 'nica@example.test');
  await waitFor(() => expect(screen.queryByText(/Ingresá un email válido/)).not.toBeInTheDocument());
  expect(requests).toHaveLength(0);
});

it('returns to the requested internal destination after login without repeating a domain mutation', async () => {
  await i18n.changeLanguage('es');
  const requests = setup('/login?returnTo=%2Fcars%3Fbrand%3D1%23reviews');
  input('Email', 'nica@example.test'); input('Contraseña', 'password123');
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  expect(await screen.findByText('Destination: /cars?brand=1#reviews')).toBeInTheDocument();
  expect(requests.every((request) => request.method === 'GET')).toBe(true);
});

it('focuses the first invalid field and makes no request for mismatching passwords', async () => {
  await i18n.changeLanguage('es');
  const requests = setup('/register');
  fillRegistration(); input('Confirmar contraseña', 'wrong');
  fireEvent.click(screen.getByRole('button', { name: 'Crear cuenta' }));
  expect(await screen.findByText('Las contraseñas no coinciden.')).toBeInTheDocument();
  expect(screen.getByLabelText('Confirmar contraseña')).toHaveFocus();
  expect(requests).toHaveLength(0);
});

it('creates once, automatically logs in, and sends only the registration resource fields', async () => {
  await i18n.changeLanguage('es');
  const requests = setup('/register'); fillRegistration();
  fireEvent.click(screen.getByRole('button', { name: 'Crear cuenta' }));
  expect(await screen.findByText('Destination: /')).toBeInTheDocument();
  const posts = requests.filter((request) => request.method === 'POST');
  expect(posts).toHaveLength(1);
  expect(await posts[0].json()).toEqual({ displayName: 'nica', email: 'nica@example.test', password: 'password123' });
  expect(posts[0].headers.get('Content-Type')).toBe('application/vnd.paw.user.v1+json');
  expect(posts[0].headers.has('Authorization')).toBe(false);
});

it('shows account created and a blank password when registration succeeds but login fails', async () => {
  await i18n.changeLanguage('es');
  const requests = setup('/register?returnTo=%2Fcars', (request) => request.headers.get('Authorization')?.startsWith('Basic ')
    ? json({ code: 'INVALID_CREDENTIALS' }, 401) : undefined);
  fillRegistration(); fireEvent.click(screen.getByRole('button', { name: 'Crear cuenta' }));
  expect(await screen.findByText(/Cuenta creada/)).toBeInTheDocument();
  expect(screen.getByLabelText('Email')).toHaveValue('nica@example.test');
  expect(screen.getByLabelText('Contraseña', { exact: true })).toHaveValue('');
  expect(requests.filter((request) => request.method === 'POST')).toHaveLength(1);
});

it('places duplicate email errors next to the field and clears submitted passwords', async () => {
  await i18n.changeLanguage('es');
  setup('/register', (request) => request.method === 'POST'
    ? json({ code: 'CONFLICT', fieldErrors: [{ field: 'email', code: 'DUPLICATE' }] }, 409) : undefined);
  fillRegistration(); fireEvent.click(screen.getByRole('button', { name: 'Crear cuenta' }));
  expect(await screen.findByText('Este email ya está registrado.')).toBeInTheDocument();
  expect(screen.getByLabelText('Email')).toHaveFocus();
  expect(screen.getByLabelText('Contraseña', { exact: true })).toHaveValue('');
});

it('does not post again when the registration response is lost', async () => {
  await i18n.changeLanguage('es');
  const requests = setup('/register', (request) => request.method === 'POST' ? Promise.reject(new TypeError('fetch failed')) : undefined);
  fillRegistration(); fireEvent.click(screen.getByRole('button', { name: 'Crear cuenta' }));
  expect(await screen.findByText(/No pudimos confirmar si se creó la cuenta/)).toBeInTheDocument();
  expect(requests.filter((request) => request.method === 'POST')).toHaveLength(1);
  expect(requests.some((request) => request.headers.has('Authorization'))).toBe(false);
});

it('blocks rapid duplicate submissions while a registration is pending', async () => {
  await i18n.changeLanguage('es');
  let finish!: (response: Response) => void;
  const pending = new Promise<Response>((resolve) => { finish = resolve; });
  const requests = setup('/register', (request) => request.method === 'POST' ? pending : undefined);
  fillRegistration();
  const form = screen.getByRole('button', { name: 'Crear cuenta' }).closest('form')!;
  fireEvent.submit(form); fireEvent.submit(form);
  await waitFor(() => expect(requests.filter((request) => request.method === 'POST')).toHaveLength(1));
  expect(screen.getByRole('button', { name: 'Procesando…' })).toBeDisabled();
  await act(async () => { finish(json({ id: '1' }, 201)); });
  expect(await screen.findByText('Destination: /')).toBeInTheDocument();
});
