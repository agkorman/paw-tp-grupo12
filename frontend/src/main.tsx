import React from 'react';
import ReactDOM from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import { router } from './app/router';
import { AuthProvider, AuthNotices } from './features/auth';
import { HttpClientProvider } from './shared/api/HttpClientProvider';
import { createAppAuth } from './app/auth';
import './app/i18n';
import './shared/styles/app.css';

const session = createAppAuth();

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <AuthProvider session={session}>
      <HttpClientProvider client={session.client}>
        <AuthNotices />
        <RouterProvider router={router} />
      </HttpClientProvider>
    </AuthProvider>
  </React.StrictMode>,
);
