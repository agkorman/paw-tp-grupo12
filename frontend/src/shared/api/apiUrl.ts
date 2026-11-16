import { ApiClientError } from './api-error.types';

/** Vite supplies this metadata; the WAR entry page can override it at deployment time. */
export function getApiBaseUrl(): string {
  const path = document.querySelector<HTMLMetaElement>('meta[name="paw-api-base-path"]')?.content;
  if (!path || !path.startsWith('/') || path.startsWith('//')) {
    throw new ApiClientError('configuration', 'Missing or invalid paw-api-base-path metadata');
  }
  return new URL(path, window.location.origin).href;
}

/** Resolve canonical links without allowing credentials to leave this API's origin/path. */
export function resolveApiUrl(href: string, baseUrl: string): string {
  try {
    const base = new URL(baseUrl);
    const root = base.pathname.replace(/\/$/, '');
    const url = new URL(href, `${base.origin}${root}/`);
    // Encoded separators can be interpreted differently by proxies and servlet containers.
    if (!['http:', 'https:'].includes(base.protocol) || !root || base.search || base.hash
        || base.username || base.password || url.origin !== base.origin || url.username || url.password
        || url.hash || /%(?:2f|5c|25)/i.test(url.pathname)
        || (url.pathname !== root && !url.pathname.startsWith(`${root}/`))) {
      throw new Error();
    }
    return url.href;
  } catch {
    throw new ApiClientError('configuration', 'URL must belong to the configured API');
  }
}
