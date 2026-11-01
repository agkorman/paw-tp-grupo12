import { createBrowserRouter } from 'react-router-dom';
import { AppLayout } from './AppLayout';

const basePath = document.querySelector<HTMLMetaElement>('meta[name="paw-spa-base-path"]')?.content || '/';

export const router = createBrowserRouter(
  [
    {
      path: '/',
      element: <AppLayout />,
      children: [
        {
          index: true,
          lazy: async () => ({ Component: (await import('./pages/HomePage')).HomePage }),
        },
        {
          path: 'cars',
          lazy: async () => ({ Component: (await import('../features/catalog/pages/CatalogPage')).CatalogPage }),
        },
      ],
    },
  ],
  { basename: basePath },
);
