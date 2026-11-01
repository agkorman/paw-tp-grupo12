import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import commonEs from './es.json';
import commonEn from './en.json';
import catalogEs from '../../features/catalog/i18n/es.json';
import catalogEn from '../../features/catalog/i18n/en.json';

void i18n.use(initReactI18next).init({
  lng: navigator.language.toLowerCase().startsWith('es') ? 'es' : 'en',
  fallbackLng: 'es',
  resources: {
    es: { common: commonEs, catalog: catalogEs },
    en: { common: commonEn, catalog: catalogEn },
  },
  defaultNS: 'common',
  interpolation: { escapeValue: false },
});

export default i18n;
