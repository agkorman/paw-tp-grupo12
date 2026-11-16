import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../providers/AuthProvider';
import { safeReturnTo } from '../model/navigation';

export function AuthNavigation() {
  const auth = useAuth(), location = useLocation(), navigate = useNavigate();
  const { t } = useTranslation('auth');
  const [closing, setClosing] = useState(false);
  if (auth.status === 'restoring' || auth.status === 'authenticating') return <span role="status">{t('form.restoring')}</span>;
  if (auth.status === 'authenticated' || auth.status === 'unavailable' || closing) return <>
    {auth.user && <span>{auth.user.displayName}</span>}
    <button type="button" disabled={closing} onClick={() => {
      setClosing(true);
      const completion = auth.logout();
      navigate('/', { replace: true });
      void completion.finally(() => setClosing(false));
    }}>{t('form.logout')}</button>
  </>;
  const destination = safeReturnTo(location.pathname + location.search + location.hash);
  return <Link to={`/login?returnTo=${encodeURIComponent(destination)}`}>{t('form.login')}</Link>;
}
