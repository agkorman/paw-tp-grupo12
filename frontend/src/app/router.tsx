import { createBrowserRouter } from 'react-router-dom';
import { AppLayout } from './AppLayout';
import { useTranslation } from 'react-i18next';

function RouteLoading() {
  const { t } = useTranslation('common');
  return <main className="site-main" role="status">{t('loading')}</main>;
}

const basePath = document.querySelector<HTMLMetaElement>('meta[name="paw-spa-base-path"]')?.content || '/';

export const router = createBrowserRouter(
  [
    {
      path: '/',
      element: <AppLayout />,
      HydrateFallback: RouteLoading,
      children: [
        {
          index: true,
          lazy: async () => ({ Component: (await import('./pages/HomePage')).HomePage }),
        },
        {
          path: 'cars',
          lazy: async () => ({ Component: (await import('../features/catalog/pages/CatalogPage')).CatalogPage }),
        },
        { path: 'login', lazy: async () => ({ Component: (await import('../features/auth/pages/LoginPage')).LoginPage }) },
        { path: 'register', lazy: async () => ({ Component: (await import('../features/auth/pages/RegisterPage')).RegisterPage }) },
      ],
    },
  ],
  { basename: basePath },
);
