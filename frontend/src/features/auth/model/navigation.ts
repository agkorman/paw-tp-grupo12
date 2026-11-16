/** Router-relative destinations only; no external URLs, API resources or auth loops. */
export function safeReturnTo(value: string | null | undefined): string {
  if (!value || !value.startsWith('/') || value.startsWith('//') || /[\\\s\u0000-\u001f]/.test(value)) return '/';
  try {
    const decoded = decodeURIComponent(value);
    if (/[\\\u0000-\u001f]/.test(decoded) || decoded.startsWith('//') || /%[0-9a-f]{2}/i.test(decoded)) return '/';
    const target = new URL(value, 'https://spa.invalid');
    const decodedTarget = new URL(decoded, 'https://spa.invalid');
    // Dot segments can produce a leading // after normalization. Router paths are case-insensitive.
    if ([target, decodedTarget].some((candidate) => candidate.origin !== 'https://spa.invalid'
        || candidate.pathname.startsWith('//')
        || /^\/(?:login|register|api|assets)(?:\/|$)/i.test(candidate.pathname))) return '/';
    return target.pathname + target.search + target.hash;
  } catch { return '/'; }
}
