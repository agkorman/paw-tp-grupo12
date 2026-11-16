import i18n from '../shared/i18n';
import catalogEs from '../features/catalog/i18n/es.json';
import catalogEn from '../features/catalog/i18n/en.json';
import authEs from '../features/auth/i18n/es.json';
import authEn from '../features/auth/i18n/en.json';

i18n.addResourceBundle('es', 'catalog', catalogEs);
i18n.addResourceBundle('en', 'catalog', catalogEn);
i18n.addResourceBundle('es', 'auth', authEs);
i18n.addResourceBundle('en', 'auth', authEn);

export default i18n;
