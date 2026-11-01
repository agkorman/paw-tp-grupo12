import { NavLink, Outlet } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

export function AppLayout() {
  const { t } = useTranslation('common');

  return (
    <div className="app-shell">
      <a className="sr-only focus:not-sr-only" href="#main-content">{t('skipToContent')}</a>
      <header className="site-header">
        <NavLink className="brand" to="/" aria-label={t('brand')}>
          <span className="brand-mark">P</span>
          <span>{t('brand')}</span>
        </NavLink>
        <nav aria-label={t('navigation')} className="site-nav">
          <NavLink end to="/">{t('home')}</NavLink>
          <NavLink to="/cars">{t('cars')}</NavLink>
        </nav>
      </header>
      <main id="main-content" className="site-main"><Outlet /></main>
      <footer className="site-footer">{t('footer')}</footer>
    </div>
  );
}
