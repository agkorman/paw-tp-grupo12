import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import commonEs from './es.json';
import commonEn from './en.json';
import { initialLocale } from './locale';

void i18n.use(initReactI18next).init({
  lng: initialLocale(() => window.localStorage, navigator.languages),
  fallbackLng: 'es',
  resources: {
    es: { common: commonEs },
    en: { common: commonEn },
  },
  defaultNS: 'common',
  interpolation: { escapeValue: false },
});

export default i18n;
