-- Community feature seed data.
-- Run after schema.sql so community search_vector triggers and indexes exist.

INSERT INTO users (username, email, password, role, preferred_locale)
VALUES (
    'tesasadsdadsfsda',
    'test@mail.com',
    '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi0OT9U.Q28Lh/5SuN4uhB1pQzU0F6u',
    'user',
    'es'
)
ON CONFLICT (email) DO UPDATE
SET username = EXCLUDED.username,
    role = EXCLUDED.role,
    preferred_locale = EXCLUDED.preferred_locale;

INSERT INTO community_topics (code)
VALUES
    ('classics'),
    ('sports'),
    ('motorsport'),
    ('offroad'),
    ('electric'),
    ('builds'),
    ('mechanical'),
    ('photography'),
    ('marketplace'),
    ('jdm'),
    ('news')
ON CONFLICT (LOWER(BTRIM(code))) DO NOTHING;

WITH owner_user AS (
    SELECT user_id FROM users WHERE email = 'test@mail.com'
), seed_communities(slug, name, description, topic_code) AS (
    VALUES
        ('seed-track-days', 'Track Days Argentina', 'Puesta a punto para track days, frenos, cubiertas y telemetria casera.', 'motorsport'),
        ('seed-electric-garage', 'Electric Garage', 'Carga, autonomia real y mantenimiento de electricos e hibridos.', 'electric'),
        ('seed-classic-restos', 'Classic Restos', 'Restauraciones de clasicos, repuestos dificiles y proyectos de largo plazo.', 'classics'),
        ('seed-jdm-night', 'JDM Night', 'Importados japoneses, swaps, stance sobrio y mecanica confiable.', 'jdm'),
        ('seed-offroad-routes', 'Offroad Routes', 'Rutas de ripio, barro, auxilio, cubiertas AT y preparacion para viajar.', 'offroad'),
        ('seed-detail-photo', 'Detail Photo Crew', 'Fotografia automotriz, lavados, detailing y sesiones de garage.', 'photography'),
        ('seed-daily-drivers', 'Daily Drivers', 'Autos de uso diario, confiabilidad, costos y confort para ciudad.', 'mechanical'),
        ('seed-market-finds', 'Market Finds', 'Oportunidades de compra, precios publicados y alertas de unidades raras.', 'marketplace'),
        ('seed-sports-cars', 'Sports Cars Club', 'Deportivos nuevos y usados, setup de calle y sensaciones de manejo.', 'sports'),
        ('seed-workshop-help', 'Workshop Help', 'Diagnostico, herramientas, fallas frecuentes y reparaciones paso a paso.', 'mechanical'),
        ('seed-project-builds', 'Project Builds', 'Proyectos armados por la comunidad, desde motor hasta interior.', 'builds'),
        ('seed-auto-news', 'Auto News Radar', 'Lanzamientos, rumores, homologaciones y novedades del mercado local.', 'news'),
        ('seed-turbo-school', 'Turbo School', 'Turbos, intercoolers, mapas conservadores y armado responsable.', 'mechanical'),
        ('seed-ev-roadtrips', 'EV Roadtrips', 'Viajes largos en electricos, paradas de carga y consumo real en ruta.', 'electric'),
        ('seed-rally-stories', 'Rally Stories', 'Historias de rally, autos historicos y preparaciones de tierra.', 'motorsport'),
        ('seed-garage-talk', 'Garage Talk', 'Charla general de garage para comparar ideas, presupuestos y experiencias.', 'builds')
)
INSERT INTO communities (slug, name, description, created_by_user_id)
SELECT sc.slug, sc.name, sc.description, ou.user_id
FROM seed_communities sc
CROSS JOIN owner_user ou
ON CONFLICT (LOWER(BTRIM(slug))) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    created_by_user_id = EXCLUDED.created_by_user_id;

WITH owner_user AS (
    SELECT user_id FROM users WHERE email = 'test@mail.com'
)
INSERT INTO community_memberships (community_id, user_id, role)
SELECT c.community_id, ou.user_id, 'moderator'
FROM communities c
CROSS JOIN owner_user ou
WHERE c.slug LIKE 'seed-%'
ON CONFLICT (community_id, user_id) DO UPDATE
SET role = EXCLUDED.role;

WITH seed_assignments(slug, topic_code) AS (
    VALUES
        ('seed-track-days', 'motorsport'),
        ('seed-track-days', 'sports'),
        ('seed-electric-garage', 'electric'),
        ('seed-electric-garage', 'news'),
        ('seed-classic-restos', 'classics'),
        ('seed-classic-restos', 'builds'),
        ('seed-jdm-night', 'jdm'),
        ('seed-jdm-night', 'sports'),
        ('seed-offroad-routes', 'offroad'),
        ('seed-offroad-routes', 'builds'),
        ('seed-detail-photo', 'photography'),
        ('seed-daily-drivers', 'mechanical'),
        ('seed-market-finds', 'marketplace'),
        ('seed-sports-cars', 'sports'),
        ('seed-workshop-help', 'mechanical'),
        ('seed-project-builds', 'builds'),
        ('seed-auto-news', 'news'),
        ('seed-turbo-school', 'mechanical'),
        ('seed-ev-roadtrips', 'electric'),
        ('seed-rally-stories', 'motorsport'),
        ('seed-garage-talk', 'builds')
)
INSERT INTO community_topic_assignments (community_id, topic_id)
SELECT c.community_id, t.topic_id
FROM seed_assignments sa
JOIN communities c ON c.slug = sa.slug
JOIN community_topics t ON t.code = sa.topic_code
ON CONFLICT (community_id, topic_id) DO NOTHING;

WITH owner_user AS (
    SELECT user_id FROM users WHERE email = 'test@mail.com'
), seed_posts(community_slug, post_slug, title, body, created_offset_hours) AS (
    VALUES
        ('seed-track-days', 'camber-or-caster-first', 'Camber o caster primero?', 'Estoy probando alineacion para track day y quiero priorizar temperatura pareja en las delanteras.', 1),
        ('seed-track-days', 'brake-fluid-test', 'Liquido de frenos para tandas', 'Comparativa practica entre DOT 4 racing y opciones faciles de conseguir localmente.', 2),
        ('seed-track-days', 'tire-pressure-notes', 'Presiones en caliente', 'Anoten presion en frio, presion al entrar y desgaste por hombro para comparar.', 3),
        ('seed-track-days', 'helmet-and-seat', 'Butaca y casco para empezar', 'Checklist minimo para entrar mas comodo y seguro sin armar un auto de carrera completo.', 4),
        ('seed-track-days', 'lap-timer-budget', 'Lap timer economico', 'Opciones con celular, GPS externo y soportes que no vibren.', 5),
        ('seed-track-days', 'cooldown-routine', 'Rutina de enfriado', 'Vueltas suaves, capot abierto y revision de fluidos despues de cada tanda.', 6),
        ('seed-track-days', 'first-track-day-pack', 'Que llevar al primer track day', 'Torque wrench, manometro, agua, cinta, precintos y una libreta para notas.', 7),
        ('seed-track-days', 'pad-wear-log', 'Registro de pastillas', 'Fotos y medicion simple para decidir cuando rotar o cambiar compuesto.', 8),
        ('seed-electric-garage', 'home-charger-size', 'Que potencia de cargador domestico conviene?', 'Experiencias reales con instalaciones monofasicas y trifasicas.', 2),
        ('seed-electric-garage', 'winter-range', 'Autonomia en invierno', 'Datos de consumo con calefaccion, ruta y ciudad.', 10),
        ('seed-classic-restos', 'rust-before-paint', 'Oxido antes de pintar', 'Como decidir entre parche parcial o panel completo en guardabarros.', 4),
        ('seed-jdm-night', 'reliable-swap-list', 'Swaps confiables para calle', 'Motores que se consiguen, repuestos y costos escondidos.', 5),
        ('seed-offroad-routes', 'recovery-kit-basics', 'Kit de rescate basico', 'Eslinga, grilletes, planchas y pala: que comprar primero.', 6),
        ('seed-detail-photo', 'golden-hour-locations', 'Lugares para fotos al atardecer', 'Fondos limpios, reflejos y permisos para no molestar.', 7),
        ('seed-daily-drivers', 'cheap-maintenance-wins', 'Mantenimiento barato que cambia todo', 'Escobillas, fluidos, alineacion y limpieza de sensores.', 8),
        ('seed-market-finds', 'price-check-template', 'Plantilla para evaluar precio', 'Kilometraje, historial, cubiertas, titularidad y margen de negociacion.', 9),
        ('seed-sports-cars', 'manual-or-auto', 'Manual o automatico para uso mixto', 'Pros y contras si el auto tambien se usa en ciudad.', 10),
        ('seed-workshop-help', 'misfire-diagnosis', 'Diagnostico de falla en cilindro', 'Orden logico: bujia, bobina, inyector, compresion.', 11),
        ('seed-project-builds', 'budget-sheet', 'Planilla de presupuesto de proyecto', 'Como separar piezas obligatorias de caprichos para no frenar el armado.', 12),
        ('seed-auto-news', 'new-hybrid-launches', 'Lanzamientos hibridos que vale mirar', 'Modelos anunciados, precios estimados y disponibilidad.', 13),
        ('seed-turbo-school', 'boost-leak-check', 'Buscar perdida de presion', 'Prueba de humo, abrazaderas y mangueras que suelen fallar.', 14),
        ('seed-ev-roadtrips', 'charging-stop-map', 'Mapa de paradas de carga', 'Ruta larga con consumo, tiempos de carga y margen de bateria.', 15),
        ('seed-rally-stories', 'gravel-setup', 'Setup para tierra', 'Altura, protecciones y neumaticos para ripio rapido.', 16),
        ('seed-garage-talk', 'three-car-garage', 'Garage ideal de tres autos', 'Uno diario, uno divertido y uno proyecto: que elegirias?', 17)
)
INSERT INTO community_posts (community_id, author_user_id, slug, title, body, created_at, updated_at)
SELECT c.community_id,
       ou.user_id,
       sp.post_slug,
       sp.title,
       sp.body,
       CURRENT_TIMESTAMP - (sp.created_offset_hours || ' hours')::interval,
       CURRENT_TIMESTAMP - (sp.created_offset_hours || ' hours')::interval
FROM seed_posts sp
JOIN communities c ON c.slug = sp.community_slug
CROSS JOIN owner_user ou
ON CONFLICT (community_id, LOWER(BTRIM(slug))) DO UPDATE
SET title = EXCLUDED.title,
    body = EXCLUDED.body,
    author_user_id = EXCLUDED.author_user_id,
    created_at = EXCLUDED.created_at,
    updated_at = EXCLUDED.updated_at,
    hidden = FALSE;

WITH owner_user AS (
    SELECT user_id FROM users WHERE email = 'test@mail.com'
), seed_comments(post_slug, body, created_offset_minutes) AS (
    VALUES
        ('camber-or-caster-first', 'Buen hilo para probar la busqueda por track y motorsport.', 15),
        ('camber-or-caster-first', 'Yo arrancaria con camber y despues ajustaria caster segun sensacion de volante.', 25),
        ('brake-fluid-test', 'Me sirve para comparar por utiles y comentarios dentro de la comunidad.', 35),
        ('tire-pressure-notes', 'La libreta de presiones despues ayuda muchisimo.', 45),
        ('home-charger-size', 'Con 7 kW alcanza para recuperar bateria durante la noche.', 55),
        ('winter-range', 'El consumo sube bastante si el auto duerme afuera.', 65),
        ('rust-before-paint', 'Si hay burbujas cerca de la union, conviene abrir mas de lo esperado.', 75),
        ('reliable-swap-list', 'Lo dificil no es el motor, es dejar todo prolijo y legal.', 85),
        ('recovery-kit-basics', 'Agregaria guantes y una manta pesada para la eslinga.', 95),
        ('golden-hour-locations', 'Los fondos simples hacen que el auto se vea mejor.', 105),
        ('misfire-diagnosis', 'Mover bobina de cilindro es la prueba mas rapida.', 115),
        ('three-car-garage', 'Diario confiable, deportivo liviano y clasico para restaurar.', 125)
)
INSERT INTO community_post_comments (post_id, user_id, body, created_at, updated_at)
SELECT p.post_id,
       ou.user_id,
       sc.body,
       CURRENT_TIMESTAMP - (sc.created_offset_minutes || ' minutes')::interval,
       CURRENT_TIMESTAMP - (sc.created_offset_minutes || ' minutes')::interval
FROM seed_comments sc
JOIN community_posts p ON p.slug = sc.post_slug
JOIN communities c ON c.community_id = p.community_id AND c.slug LIKE 'seed-%'
CROSS JOIN owner_user ou
WHERE NOT EXISTS (
    SELECT 1
    FROM community_post_comments existing
    WHERE existing.post_id = p.post_id
      AND existing.user_id = ou.user_id
      AND existing.body = sc.body
);

WITH owner_user AS (
    SELECT user_id FROM users WHERE email = 'test@mail.com'
), helpful_posts(post_slug) AS (
    VALUES
        ('camber-or-caster-first'),
        ('brake-fluid-test'),
        ('home-charger-size'),
        ('rust-before-paint'),
        ('misfire-diagnosis'),
        ('three-car-garage')
)
INSERT INTO community_post_helpful_reactions (post_id, user_id)
SELECT p.post_id, ou.user_id
FROM helpful_posts hp
JOIN community_posts p ON p.slug = hp.post_slug
JOIN communities c ON c.community_id = p.community_id AND c.slug LIKE 'seed-%'
CROSS JOIN owner_user ou
ON CONFLICT (post_id, user_id) DO NOTHING;

UPDATE communities
SET search_vector = communities_build_search_vector(name, slug, description)
WHERE slug LIKE 'seed-%';
