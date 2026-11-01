import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

export function HomePage() {
  const { t } = useTranslation('common');

  return (
    <section className="hero">
      <p className="eyebrow">{t('eyebrow')}</p>
      <h1>{t('heroTitle')}</h1>
      <p className="hero-copy">{t('heroDescription')}</p>
      <Link className="primary-link" to="/cars">{t('viewCars')}</Link>
    </section>
  );
}
