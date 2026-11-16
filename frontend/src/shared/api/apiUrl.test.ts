import { afterEach, expect, it } from 'vitest';
import { getApiBaseUrl, resolveApiUrl } from './apiUrl';

afterEach(() => document.querySelector('meta[name="paw-api-base-path"]')?.remove());

it('reads deployment metadata without inferring a context from SPA routes', () => {
  const meta = document.createElement('meta');
  meta.name = 'paw-api-base-path';
  meta.content = '/garage/api';
  document.head.appendChild(meta);

  const baseUrl = getApiBaseUrl();

  expect(baseUrl).toBe(`${window.location.origin}/garage/api`);
  expect(resolveApiUrl('/garage/api/users/1', baseUrl)).toBe(`${window.location.origin}/garage/api/users/1`);
});

it('fails explicitly when deployment metadata is missing', () => {
  const readConfig = getApiBaseUrl;

  expect(readConfig).toThrow('Missing or invalid paw-api-base-path metadata');
});

it('supports deploying the WAR at the root context', () => {
  const baseUrl = 'https://paw.test/api';

  const url = resolveApiUrl('/api/users/1', baseUrl);

  expect(url).toBe('https://paw.test/api/users/1');
});
