import { defineConfig } from 'vitest/config';
import { loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '.', 'PAW_');
  const apiContextPath = (env.PAW_DEV_API_CONTEXT_PATH || '/webapp').replace(/\/$/, '');

  return {
    base: './',
    plugins: [react()],
    server: {
      proxy: {
        '/api': {
          target: env.PAW_DEV_API_ORIGIN || 'http://localhost:8080',
          changeOrigin: true,
          rewrite: (path) => `${apiContextPath}${path}`,
        },
      },
    },
    test: {
      environment: 'jsdom',
      setupFiles: ['./src/test/setup.ts'],
    },
  };
});
