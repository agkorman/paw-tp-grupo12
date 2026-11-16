/** Router-relative destinations only; no external URLs, API resources or auth loops. */
export function safeReturnTo(value: string | null | undefined): string {
  if (!value || !value.startsWith('/') || value.startsWith('//') || /[\\\s\u0000-\u001f]/.test(value)) return '/';
  try {
    const decoded = decodeURIComponent(value);
    if (/[\\\u0000-\u001f]/.test(decoded) || decoded.startsWith('//') || /%[0-9a-f]{2}/i.test(decoded)) return '/';
    const target = new URL(value, 'https://spa.invalid');
    if (target.origin !== 'https://spa.invalid' || /^\/(?:login|register|api|assets)(?:\/|$)/.test(target.pathname)
        || /^\/(?:login|register|api|assets)(?:\/|$)/.test(new URL(decoded, 'https://spa.invalid').pathname)) return '/';
    return target.pathname + target.search + target.hash;
  } catch { return '/'; }
}
