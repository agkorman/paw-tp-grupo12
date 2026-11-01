import { useTranslation } from 'react-i18next';

export function CatalogPage() {
  const { t } = useTranslation('catalog');

  return (
    <section className="page-intro">
      <p className="eyebrow">{t('eyebrow')}</p>
      <h1>{t('title')}</h1>
      <p>{t('pending')}</p>
    </section>
  );
}
