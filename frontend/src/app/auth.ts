import { createAuthSession, createAuthStorage } from '../features/auth';
import { getApiBaseUrl } from '../shared/api/apiUrl';
import { createHttpClient } from '../shared/api/httpClient';
import { saveLocale } from '../shared/i18n/locale';
import i18n from './i18n';

export function createAppAuth() {
  const baseUrl = getApiBaseUrl();
  return createAuthSession({ baseUrl,
    storage: createAuthStorage(baseUrl, { local: () => window.localStorage, session: () => window.sessionStorage }),
    createClient: (authentication) => createHttpClient({ baseUrl, authentication }),
    applyLocale: (locale) => { void i18n.changeLanguage(locale); },
    saveLocalLocale: saveLocale,
  });
}
