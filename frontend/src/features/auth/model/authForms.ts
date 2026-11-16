import { z } from 'zod';
import { ApiClientError } from '../../../shared/api/api-error.types';

const email = z.string().trim().min(1, 'validation.required').max(100, 'validation.email')
  .regex(/^[^\s@]+@[^\s@]+$/, 'validation.email');
const password = z.string().min(1, 'validation.required');
export const loginSchema = z.object({ email, password, rememberMe: z.boolean(), displayName: z.string(), confirmPassword: z.string() });
export const registrationSchema = z.object({ email,
  displayName: z.string().trim().min(1, 'validation.required').max(50, 'validation.displayName')
    .regex(/^[A-Za-z0-9._-]+$/, 'validation.displayName'),
  password: password.refine((value) => value.trim().length > 0 && value.length >= 8, 'validation.passwordMin')
    .refine((value) => new TextEncoder().encode(value).length <= 72, 'validation.passwordMax'),
  confirmPassword: z.string().min(1, 'validation.required'), rememberMe: z.boolean(),
}).refine((value) => value.password === value.confirmPassword, { path: ['confirmPassword'], error: 'validation.passwordMatch' });
export type LoginValues = z.infer<typeof loginSchema>;
export type RegistrationValues = z.infer<typeof registrationSchema>;

export function formErrorKey(error: unknown): string {
  if (!(error instanceof ApiClientError)) return 'errors.unavailable';
  if (error.code === 'REGISTRATION_UNCONFIRMED') return 'errors.registrationUnconfirmed';
  if (error.code === 'INVALID_CREDENTIALS') return 'errors.invalidCredentials';
  if (error.code === 'AUTH_STORAGE_UNAVAILABLE') return 'notices.storageUnavailable';
  if (error.kind === 'network' || error.kind === 'timeout') return 'errors.connection';
  return 'errors.unavailable';
}

export function fieldErrorKey(field: string, code: string): string {
  if (code === 'DUPLICATE') return field === 'email' ? 'validation.emailDuplicate' : 'validation.displayNameDuplicate';
  if (code === 'NotBlank') return 'validation.required';
  if (field === 'email' && ['Size', 'Email'].includes(code)) return 'validation.email';
  if (field === 'displayName' && ['Size', 'Pattern'].includes(code)) return 'validation.displayName';
  if (field === 'password' && code === 'Utf8Size') return 'validation.passwordMax';
  if (field === 'password' && code === 'Size') return 'validation.passwordSize';
  return 'validation.invalid';
}
