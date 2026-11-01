import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { expect, it } from 'vitest';
import { AppLayout } from './AppLayout';
import '../shared/i18n';

it('navigates from the shared layout to the catalog route', () => {
  // Arrange
  render(
    <MemoryRouter initialEntries={['/']}>
      <Routes>
        <Route element={<AppLayout />} path="/">
          <Route index element={<div>Home route</div>} />
          <Route path="cars" element={<div>Catalog route</div>} />
        </Route>
      </Routes>
    </MemoryRouter>,
  );

  // Exercise
  fireEvent.click(screen.getByRole('link', { name: /^(Autos|Cars)$/ }));

  // Assertions
  expect(screen.getByText('Catalog route')).toBeInTheDocument();
});
