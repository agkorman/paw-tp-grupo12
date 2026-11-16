import { useTranslation } from 'react-i18next';
import { useAuth } from '../providers/AuthProvider';
import './auth-status.css';

/** Persistent auth-state feedback; the login/register forms are implemented separately. */
export function AuthNotices() {
  const { notices, status, retryProfile } = useAuth();
  const { t } = useTranslation('auth');
  if (!notices.length) return null;
  return <aside className="auth-status" aria-live="polite">
    {notices.map((notice) => <p key={notice}>{t(`notices.${notice}`)}</p>)}
    {status === 'unavailable' && <button type="button" onClick={() => { void retryProfile().catch(() => {}); }}>
      {t('retryProfile')}
    </button>}
  </aside>;
}
