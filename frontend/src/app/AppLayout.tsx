import { Link, NavLink, Outlet, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { AuthNavigation } from '../features/auth';

export function AppLayout() {
  const { t } = useTranslation('common');
  const location = useLocation();

  return (
    <div className="app-shell">
      <Link className="sr-only focus:not-sr-only" to={{ pathname: location.pathname, search: location.search, hash: '#main-content' }}>{t('skipToContent')}</Link>
      <header className="site-header">
        <NavLink className="brand" to="/" aria-label={t('brand')}>
          <span className="brand-mark">P</span>
          <span>{t('brand')}</span>
        </NavLink>
        <nav aria-label={t('navigation')} className="site-nav">
          <NavLink end to="/">{t('home')}</NavLink>
          <NavLink to="/cars">{t('cars')}</NavLink>
          <AuthNavigation />
        </nav>
      </header>
      <main id="main-content" className="site-main"><Outlet /></main>
      <footer className="site-footer">{t('footer')}</footer>
    </div>
  );
}
