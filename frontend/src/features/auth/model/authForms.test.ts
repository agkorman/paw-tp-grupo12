import { expect, it } from 'vitest';
import { safeReturnTo } from './navigation';
import { loginSchema, registrationSchema } from './authForms';

const values = { displayName: 'nica', email: ' nica@example.test ', password: '  password  ', confirmPassword: '  password  ', rememberMe: false };

it('trims identifiers without modifying the password', () => {
  const result = registrationSchema.parse(values);
  expect(result.email).toBe('nica@example.test');
  expect(result.password).toBe('  password  ');
});

it('enforces the BCrypt byte limit and confirmation', () => {
  expect(registrationSchema.safeParse({ ...values, password: 'é'.repeat(37), confirmPassword: 'é'.repeat(37) }).success).toBe(false);
  expect(registrationSchema.safeParse({ ...values, confirmPassword: 'different' }).success).toBe(false);
  expect(registrationSchema.safeParse({ ...values, password: 'é'.repeat(36), confirmPassword: 'é'.repeat(36) }).success).toBe(true);
});

it('does not impose new registration password rules on existing accounts during login', () => {
  expect(loginSchema.safeParse({ ...values, password: 'old', displayName: '', confirmPassword: '' }).success).toBe(true);
});

it.each(['https://other.test', '//other.test', '/\\other.test', '/%2fother.test', '/%252fother.test',
  '/login', '/%6cogin', '/register?returnTo=/login', '/cars/../login', '/api/users', '/assets/app.js', '/x%0d%0aLocation:evil'])('rejects unsafe or looping return destination %s', (value) => {
  expect(safeReturnTo(value)).toBe('/');
});

it('preserves internal query parameters and fragments', () => {
  expect(safeReturnTo('/cars?brand=1#reviews')).toBe('/cars?brand=1#reviews');
});
