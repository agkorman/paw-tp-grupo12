import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { expect, it } from 'vitest';
import { AppLayout } from './AppLayout';
import '../shared/i18n';
import { AuthProvider, createAuthSession } from '../features/auth';
import { createHttpClient } from '../shared/api/httpClient';

it('navigates from the shared layout to the catalog route', () => {
  // Arrange
  const session = createAuthSession({ baseUrl: 'http://localhost/api',
    storage: { read: () => undefined, save: () => {}, clear: () => true },
    createClient: (authentication) => createHttpClient({ baseUrl: 'http://localhost/api', authentication }),
    applyLocale: () => {}, saveLocalLocale: () => {},
  });
  render(
    <AuthProvider session={session}>
    <MemoryRouter initialEntries={['/']}>
      <Routes>
        <Route element={<AppLayout />} path="/">
          <Route index element={<div>Home route</div>} />
          <Route path="cars" element={<div>Catalog route</div>} />
        </Route>
      </Routes>
    </MemoryRouter>
    </AuthProvider>,
  );

  // Exercise
  fireEvent.click(screen.getByRole('link', { name: /^(Autos|Cars)$/ }));

  // Assertions
  expect(screen.getByText('Catalog route')).toBeInTheDocument();
});
