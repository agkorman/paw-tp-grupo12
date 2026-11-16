import { afterEach, expect, it } from 'vitest';
import { initialLocale } from './locale';

afterEach(() => localStorage.clear());

it('prefers the explicit local choice over browser languages', () => {
  localStorage.setItem('paw.locale', 'es');

  const locale = initialLocale(() => localStorage, ['en-US']);

  expect(locale).toBe('es');
});

it('uses the first supported browser language and falls back to Spanish', () => {
  const languages = ['fr-FR', 'en-GB'];

  const locale = initialLocale(() => localStorage, languages);

  expect(locale).toBe('en');
  expect(initialLocale(() => localStorage, ['fr-FR'])).toBe('es');
});

it('does not require storage access to choose a language', () => {
  const blocked = () => { throw new Error('blocked'); };

  const locale = initialLocale(blocked, ['en-US']);

  expect(locale).toBe('en');
});
