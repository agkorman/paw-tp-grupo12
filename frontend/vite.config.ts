import { defineConfig } from 'vitest/config';
import { loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode, command }) => {
  const env = loadEnv(mode, '.', 'PAW_');
  const contextPath = (command === 'serve' ? env.PAW_DEV_API_CONTEXT_PATH : undefined)
    ?? env.PAW_API_CONTEXT_PATH ?? '/webapp';
  const apiContextPath = contextPath.replace(/\/$/, '');
  if (apiContextPath && !/^\/(?:[a-zA-Z0-9_-]+\/)*[a-zA-Z0-9_-]+$/.test(apiContextPath)) {
    throw new Error('PAW_API_CONTEXT_PATH / PAW_DEV_API_CONTEXT_PATH must be an absolute context path or empty');
  }
  const apiPath = `${apiContextPath}/api`;

  return {
    base: './',
    plugins: [react(), {
      name: 'paw-api-config',
      transformIndexHtml: () => [{
        tag: 'meta', attrs: { name: 'paw-api-base-path', content: apiPath }, injectTo: 'head',
      }],
    }],
    server: {
      proxy: {
        [`^${apiPath}(?:/|\\?|$)`]: {
          target: env.PAW_DEV_API_ORIGIN || 'http://localhost:8080',
          changeOrigin: false,
        },
      },
    },
    test: {
      environment: 'jsdom',
      setupFiles: ['./src/test/setup.ts'],
    },
  };
});
