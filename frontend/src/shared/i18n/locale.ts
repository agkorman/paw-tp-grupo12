export type Locale = 'es' | 'en';
const key = 'paw.locale';

export function initialLocale(storage: () => Storage, languages: readonly string[]): Locale {
  try {
    const local = storage().getItem(key);
    if (local === 'es' || local === 'en') return local;
  } catch { /* Language preferences are optional; auth persistence has its own stricter policy. */ }
  for (const language of languages) {
    const base = language.toLowerCase().split('-')[0];
    if (base === 'es' || base === 'en') return base;
  }
  return 'es';
}

export function saveLocale(locale: Locale) {
  try { window.localStorage.setItem(key, locale); } catch { /* Current-page language still works. */ }
}
