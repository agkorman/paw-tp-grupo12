# Activity Feed Mixto Sin Tabs

  ## Resumen

  - Convertir /activity en una sola vista de “latest” sin tabs, sin filtros y sin búsqueda de usuarios.
  - Reemplazar activity-review-card.tag por un activity-card.tag genérico, con layout tipo community-post-card: header de autor/tiempo, metadata por tipo, título, body e
    image-gallery debajo del body.
  - Mezclar reviews y community posts en un único feed cronológico (createdAt desc) con paginación normal y navegación directa al detalle.
  - No habrá preview lateral por ahora. El código actual de preview puede quedar en repo para reutilizarlo después, pero desconectado de la página actual.

  ## Cambios de implementación

  - Backend:
      - Crear un ActivityService dedicado en service-contracts/services con un único caso de uso: obtener el feed mixto paginado de /activity.
      - El servicio debe devolver un Page<ActivityFeedItem> tipado, no Review ni CommunityPost sueltos.
      - Definir ActivityFeedItem en model como read model de actividad, con tipo (review / communityPost) y los datos ya hidratados que la UI necesita:
          - para review: Review, Car, reviewPage, List<ReviewImage>
          - para community post: CommunityPost, helpfulCount, commentCount, List<CommunityPostImage>
      - Retirar del flujo de activity la API específica de reviews (ReviewService#getActivityFeedReviews y sus constantes de tabs) si queda sin usos.
      - Agregar ActivityDao en persistence-contracts/persistence para resolver la paginación mixta correctamente.
      - ActivityDao debe hacer la paginación por referencias con SQL nativo:
          - contar reviews + community_posts.hidden = false
          - consultar refs mezcladas con UNION ALL, ordenadas por created_at DESC y con tie-break determinístico estable
      - La hidratación debe respetar el patrón 1+1 del repo:
          - refs paginadas desde ActivityDao
          - reviews completas por ReviewDao.findByIds(...)
          - posts completos por un nuevo CommunityDao.findPostsByIds(...)
          - autos por CarDao.findByIds(...)
          - imágenes de reviews por ReviewImageDao.findAllByReviewIds(...)
          - imágenes de posts por CommunityPostImageDao.findAllByPostIds(...)
          - counts de community posts con los métodos existentes de comments/helpful
          - páginas-ancla de reviews con ReviewDao.findDefaultPagesByReviewIds(...)
      - Mantener /activity como GET público, sin cambios de seguridad.
  - Web/controller/view:
      - Simplificar ActivityController para que acepte solo page; eliminar tab, normalización de tabs y counts de latest/following/favorites.
      - Reemplazar activityReviews por una colección genérica de items de actividad.
      - Quitar de activity.jsp:
          - subtabs
          - form de búsqueda de usuarios
          - import y uso de activity-tab-panel.tag
          - import de activity.js
          - import de review-preview.css
      - Mantener paginación simple sobre /activity?page=N.
      - Crear activity-card.tag genérico y retirar activity-review-card.tag.
      - La card debe renderizarse como <article>, no como <a> raíz, porque image-gallery contiene botones.
      - Navegación al detalle:
          - link en el título
          - CTA/link visible al detalle
      - Contenido de activity-card:
          - común: autor, tiempo relativo, título, body, galería debajo del body
          - review: mostrar nombre del auto + badge de rating; sin imagen de fondo; la galería usa imágenes de la review si existen, y si no hay, no se muestra galería
          - community post: mostrar comunidad de origen + métricas helpful/comments
      - Incluir en activity.jsp los shared assets de galería:
          - /css/image-lightbox.css
          - pa:image-lightbox
          - /js/shared/image-lightbox.js
      - Mantener el código viejo de preview (activity-review-preview-panel.tag, activity.js, selectors asociados) en repo pero sin referenciarlo desde /activity.
  - CSS/i18n:
      - Refactorizar activity.css para clases genéricas activity-card-* en lugar de activity-review-* para la parte activa de la página.
      - Eliminar del layout actual las reglas de tabs, preview activa y búsqueda de usuarios que ya no se usen en /activity.
      - Agregar claves i18n nuevas en ambos bundles para:
          - labels/aria de card genérica
          - metadata de review y community post
          - CTA de navegación al detalle
          - estado vacío único del feed
      - Eliminar o dejar sin uso las keys de tabs/filtros solo si dejan de estar referenciadas.

  ## Tests

  - Persistencia:
      - nuevo test de ActivityJpaDao para feed mixto con orden global por fecha, exclusión de posts ocultos y paginación estable entre páginas
      - test de CommunityDao.findPostsByIds(...) con orden preservado e hidratación de autor
  - Servicios:
      - ActivityServiceImplTest cubriendo:
          - feed vacío
          - mezcla de reviews y posts en orden cronológico
          - review con imágenes
          - review sin imágenes
          - community post con métricas e imágenes
          - cálculo correcto de reviewPage
  - Web:
      - reemplazar ActivityControllerTest por casos acordes al nuevo flujo:
          - /activity renderiza activity.jsp
          - no expone activeTab, counts ni búsqueda
          - expone items mixtos y paginación
      - eliminar tests viejos de following, favorites y selección de tabs.

  ## Supuestos y defaults

  - activity queda solo como feed “latest”; no hay filtros ni variantes autenticadas por ahora.
  - La paginación mantiene el tamaño actual del feed; conviene centralizarlo en una constante compartida de paginación para no duplicarlo entre capas.
  - El orden es global por createdAt desc; los empates se resuelven con un tie-break técnico estable, no visible para usuario.
  - Las cards no usan preview lateral ni imagen de fondo.
  - El código viejo de preview/tab puede quedar desconectado en repo para reutilización futura, pero no debe seguir importado ni ejecutándose en /activity.