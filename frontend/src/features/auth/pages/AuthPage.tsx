import { useEffect, useRef, useState } from 'react';
import { Link, Navigate, useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { ApiClientError, isRecord } from '../../../shared/api/api-error.types';
import { useHttpClient } from '../../../shared/api/HttpClientProvider';
import { registerUser } from '../../users';
import { useAuth } from '../providers/AuthProvider';
import { fieldErrorKey, formErrorKey, loginSchema, registrationSchema, type RegistrationValues } from '../model/authForms';
import './auth-form.css';
import { safeReturnTo } from '../model/navigation';

export function AuthPage({ registration = false }: { registration?: boolean }) {
  const { t } = useTranslation('auth');
  const auth = useAuth(), client = useHttpClient();
  const location = useLocation(), navigate = useNavigate();
  const [search] = useSearchParams();
  const returnTo = safeReturnTo(search.get('returnTo'));
  const created = isRecord(location.state) && location.state.accountCreated === true;
  const initialEmail = created && isRecord(location.state) && typeof location.state.email === 'string' ? location.state.email : '';
  const [visible, setVisible] = useState(false);
  const [sending, setSending] = useState(false);
  const submitting = useRef(false), active = useRef(true);
  const { register, handleSubmit, formState: { errors, isSubmitting }, setError, setValue } = useForm<RegistrationValues>({
    // Login intentionally validates only login fields; registration rules must not reject existing passwords.
    resolver: zodResolver(registration ? registrationSchema : loginSchema),
    mode: 'onTouched', reValidateMode: 'onChange',
    defaultValues: { email: initialEmail, password: '', confirmPassword: '', displayName: '', rememberMe: false },
  });
  useEffect(() => { active.current = true; return () => { active.current = false; }; }, []);
  useEffect(() => {
    if (!registration && created) navigate(location.pathname + location.search, { replace: true, state: null });
    // The one-time confirmation is retained by the mounted form, but not by browser history.
  }, []);
  const [accountCreated] = useState(created);
  const busy = sending || isSubmitting || auth.status === 'restoring' || auth.status === 'authenticating';

  async function submit(values: RegistrationValues) {
    if (submitting.current) return;
    submitting.current = true;
    setSending(true);
    try {
      if (registration) await registerUser(client, values);
      if (!active.current) return;
      try { await auth.login(values.email, values.password, values.rememberMe); }
      catch (error) {
        if (registration && active.current) {
          navigate(`/login?returnTo=${encodeURIComponent(returnTo)}`, { replace: true,
            state: { accountCreated: true, email: values.email } });
          return;
        }
        throw error;
      }
      if (active.current) navigate(returnTo, { replace: true });
    } catch (error) {
      if (!active.current) return;
      const fields = error instanceof ApiClientError ? error.fieldErrors : [];
      let focused = false;
      for (const field of fields) {
        if (!['displayName', 'email', 'password'].includes(field.field)) continue;
        setError(field.field as 'displayName' | 'email' | 'password', { type: 'server', message: fieldErrorKey(field.field, field.code) }, { shouldFocus: !focused });
        focused = true;
      }
      if (!focused) setError('root.server', { message: formErrorKey(error) });
    } finally {
      values.password = '';
      values.confirmPassword = '';
      if (active.current) { setValue('password', ''); setValue('confirmPassword', ''); setSending(false); }
      submitting.current = false;
    }
  }

  if (auth.status === 'authenticated' && !isSubmitting && !sending) return <Navigate to={returnTo} replace />;
  const error = (field: 'displayName' | 'email' | 'password' | 'confirmPassword') => errors[field]?.message
    ? <p className="auth-field-error" id={`${field}-error`}>{t(errors[field]!.message!)}</p> : null;
  const inputProps = (field: 'displayName' | 'email' | 'password' | 'confirmPassword') => ({
    id: field, readOnly: busy, 'aria-invalid': Boolean(errors[field]), 'aria-describedby': errors[field] ? `${field}-error` : undefined,
  });
  return <section className="auth-panel" aria-labelledby="auth-heading">
    <p className="eyebrow">{t('form.eyebrow')}</p>
    <h1 id="auth-heading">{t(registration ? 'form.registerTitle' : 'form.loginTitle')}</h1>
    {accountCreated && <p role="status">{t('form.accountCreated')}</p>}
    {auth.status === 'restoring' && <p role="status">{t('form.restoring')}</p>}
    <form noValidate onSubmit={handleSubmit(submit)} aria-busy={busy}>
      {errors.root?.server?.message && <p role="alert" className="auth-field-error">{t(errors.root.server.message)}</p>}
      <fieldset disabled={auth.status === 'restoring'}>
        {registration && <div className="auth-field"><label htmlFor="displayName">{t('form.displayName')}</label>
          <input {...register('displayName')} {...inputProps('displayName')} autoComplete="nickname" />{error('displayName')}</div>}
        <div className="auth-field"><label htmlFor="email">{t('form.email')}</label>
          <input {...register('email')} {...inputProps('email')} type="email" autoComplete="email" />{error('email')}</div>
        <div className="auth-field"><label htmlFor="password">{t('form.password')}</label>
          <input {...register('password')} {...inputProps('password')} type={visible ? 'text' : 'password'} autoComplete={registration ? 'new-password' : 'current-password'} />{error('password')}</div>
        {registration && <div className="auth-field"><label htmlFor="confirmPassword">{t('form.confirmPassword')}</label>
          <input {...register('confirmPassword')} {...inputProps('confirmPassword')} type={visible ? 'text' : 'password'} autoComplete="new-password" />{error('confirmPassword')}</div>}
        <button className="auth-text-button" type="button" disabled={busy} aria-pressed={visible} onClick={() => setVisible(!visible)}>{t(visible ? 'form.hidePassword' : 'form.showPassword')}</button>
        <label className="auth-check"><input {...register('rememberMe')} type="checkbox" disabled={busy} />{t('form.rememberMe')}</label>
        <button className="auth-submit" type="submit" disabled={busy}>{t(sending || isSubmitting ? 'form.submitting' : registration ? 'form.register' : 'form.login')}</button>
      </fieldset>
    </form>
    <p className="auth-other"><Link to={`${registration ? '/login' : '/register'}?returnTo=${encodeURIComponent(returnTo)}`}>{t(registration ? 'form.hasAccount' : 'form.noAccount')}</Link></p>
  </section>;
}
