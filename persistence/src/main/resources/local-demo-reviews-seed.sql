-- Reviews-only local seed for testing review tags and the recommendation wizard.
-- Run after schema.sql. It can also be run after local-demo-seed.sql.
--
-- Demo login password for every user in this file: password

BEGIN;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'cars' AND column_name = 'year'
    ) THEN
        RAISE EXCEPTION 'local-demo-reviews-seed.sql requires cars.year. Run persistence/src/main/resources/schema.sql first.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'reviews' AND column_name = 'user_id'
    ) THEN
        RAISE EXCEPTION 'local-demo-reviews-seed.sql requires the current schema. Run persistence/src/main/resources/schema.sql first.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'review_tag_assignments'
    ) THEN
        RAISE EXCEPTION 'local-demo-reviews-seed.sql requires review tag tables. Run persistence/src/main/resources/schema.sql first.';
    END IF;
END $$;

INSERT INTO users (username, email, password, role) VALUES
    ('demo_driver', 'driver.demo@laposta.local', '$2a$10$rdoHKtTSqMiCADYlWBmBHeCdZgt4VXlw4QeNPHb9Aw8PCXjmI1Ib2', 'user'),
    ('demo_eva', 'eva.demo@laposta.local', '$2a$10$rdoHKtTSqMiCADYlWBmBHeCdZgt4VXlw4QeNPHb9Aw8PCXjmI1Ib2', 'user'),
    ('demo_marco', 'marco.demo@laposta.local', '$2a$10$rdoHKtTSqMiCADYlWBmBHeCdZgt4VXlw4QeNPHb9Aw8PCXjmI1Ib2', 'user'),
    ('demo_lucia', 'lucia.demo@laposta.local', '$2a$10$rdoHKtTSqMiCADYlWBmBHeCdZgt4VXlw4QeNPHb9Aw8PCXjmI1Ib2', 'user'),
    ('demo_santi', 'santi.demo@laposta.local', '$2a$10$rdoHKtTSqMiCADYlWBmBHeCdZgt4VXlw4QeNPHb9Aw8PCXjmI1Ib2', 'user'),
    ('demo_nadia', 'nadia.demo@laposta.local', '$2a$10$rdoHKtTSqMiCADYlWBmBHeCdZgt4VXlw4QeNPHb9Aw8PCXjmI1Ib2', 'user')
ON CONFLICT (email) DO UPDATE SET
    password = EXCLUDED.password,
    role = EXCLUDED.role;

INSERT INTO review_tags (code, label_es, sentiment, dimension) VALUES
    ('low_fuel_consumption',  'Consume poco',              'positive', 'fuel_consumption'),
    ('high_fuel_consumption', 'Consume mucho',             'negative', 'fuel_consumption'),
    ('comfortable',           'Comodo',                    'positive', 'comfort'),
    ('uncomfortable',         'Incomodo',                  'negative', 'comfort'),
    ('cheap_maintenance',     'Barato de mantener',        'positive', 'maintenance_cost'),
    ('expensive_maintenance', 'Mantenimiento caro',        'negative', 'maintenance_cost'),
    ('hard_to_find_parts',    'Repuestos caros/dificiles', 'negative', 'parts_availability'),
    ('safe',                  'Seguro',                    'positive', 'safety'),
    ('big_trunk',             'Buen baul',                 'positive', 'trunk_size'),
    ('small_trunk',           'Baul chico',                'negative', 'trunk_size'),
    ('good_for_city',         'Bueno para ciudad',         'positive', 'city'),
    ('good_for_highway',      'Bueno para ruta',           'positive', 'highway'),
    ('bad_for_highway',       'Malo para ruta',            'negative', 'highway'),
    ('good_first_car',        'Buen primer auto',          'positive', 'first_car'),
    ('good_resale',           'Buena reventa',             'positive', 'resale'),
    ('bad_resale',            'Mala reventa',              'negative', 'resale'),
    ('agile_engine',          'Motor agil',                'positive', 'agility'),
    ('underpowered',          'Le falta potencia',         'negative', 'agility'),
    ('easy_to_park',          'Facil de estacionar',       'positive', 'parking'),
    ('hard_to_park',          'Dificil de estacionar',     'negative', 'parking'),
    ('noisy_cabin',           'Mucho ruido interior',      'negative', 'cabin_noise'),
    ('poor_tech',             'Tecnologia pobre',          'negative', 'tech')
ON CONFLICT (code) DO NOTHING;

CREATE TEMP TABLE demo_review_seed (
    reviewer_email text NOT NULL,
    brand_name text NOT NULL,
    model text NOT NULL,
    car_year integer NOT NULL,
    rating numeric(3,1) NOT NULL,
    title text NOT NULL,
    body text NOT NULL,
    ownership_status text,
    reviewed_model_year integer NOT NULL,
    mileage_km integer NOT NULL,
    would_recommend boolean,
    created_at timestamptz NOT NULL,
    tag_codes text[] NOT NULL
) ON COMMIT DROP;

INSERT INTO demo_review_seed VALUES
    ('driver.demo@laposta.local', 'Toyota', 'GR Supra', 2026, 4.5, 'Demo tags: Supra fast and focused', 'Tiene respuesta inmediata, buena posicion de manejo y se siente mas chico de lo que parece.', 'Propietario actual', 2026, 8200, true, CURRENT_TIMESTAMP - INTERVAL '1 day', ARRAY['agile_engine','good_for_highway','good_resale']),
    ('eva.demo@laposta.local', 'Toyota', 'GR Supra', 2026, 3.5, 'Demo tags: Supra tight cabin', 'Muy divertido, pero el habitaculo es bajo y el espacio de carga no sobra.', 'Ex propietario', 2025, 21000, true, CURRENT_TIMESTAMP - INTERVAL '3 days', ARRAY['agile_engine','small_trunk','uncomfortable']),
    ('marco.demo@laposta.local', 'Toyota', 'GR Supra', 2026, 4.0, 'Demo tags: Supra weekend car', 'Excelente para ruta de curvas, menos convincente para ciudad por visibilidad y suspensiones.', 'Propietario actual', 2026, 11800, true, CURRENT_TIMESTAMP - INTERVAL '5 days', ARRAY['agile_engine','good_for_highway','hard_to_park']),

    ('lucia.demo@laposta.local', 'Ford', 'Mustang', 2026, 4.0, 'Demo tags: Mustang character', 'El V8 empuja fuerte y hace especial cada salida. Consume mucho si se usa alegre.', 'Propietario actual', 2026, 9600, true, CURRENT_TIMESTAMP - INTERVAL '2 days', ARRAY['agile_engine','high_fuel_consumption','good_for_highway']),
    ('santi.demo@laposta.local', 'Ford', 'Mustang', 2026, 3.5, 'Demo tags: Mustang daily tradeoffs', 'Comodo adelante y con baul aceptable, pero no es facil de estacionar.', 'Propietario actual', 2026, 17800, true, CURRENT_TIMESTAMP - INTERVAL '6 days', ARRAY['comfortable','big_trunk','hard_to_park']),
    ('nadia.demo@laposta.local', 'Ford', 'Mustang', 2026, 4.0, 'Demo tags: Mustang resale', 'Se vende rapido y los repuestos comunes aparecen, aunque los neumaticos son caros.', 'Ex propietario', 2025, 32000, true, CURRENT_TIMESTAMP - INTERVAL '9 days', ARRAY['good_resale','expensive_maintenance','agile_engine']),

    ('driver.demo@laposta.local', 'Mazda', 'MX-5 Miata', 2026, 4.5, 'Demo tags: Miata pure fun', 'Liviano, manual y facil de ubicar en cualquier calle. Ideal para aprender a manejar bien.', 'Propietario actual', 2026, 5400, true, CURRENT_TIMESTAMP - INTERVAL '4 days', ARRAY['agile_engine','easy_to_park','good_first_car','cheap_maintenance','low_fuel_consumption']),
    ('eva.demo@laposta.local', 'Mazda', 'MX-5 Miata', 2026, 3.5, 'Demo tags: Miata tiny trunk', 'El baul limita viajes largos y en autopista entra ruido, pero en curvas es excelente.', 'Ex propietario', 2025, 26000, true, CURRENT_TIMESTAMP - INTERVAL '8 days', ARRAY['small_trunk','noisy_cabin','agile_engine','bad_for_highway']),
    ('marco.demo@laposta.local', 'Mazda', 'MX-5 Miata', 2026, 4.0, 'Demo tags: Miata cheap joy', 'No pide grandes gastos y la mecanica es simple comparada con deportivos mas potentes.', 'Propietario actual', 2026, 14400, true, CURRENT_TIMESTAMP - INTERVAL '12 days', ARRAY['cheap_maintenance','agile_engine','easy_to_park','low_fuel_consumption']),

    ('lucia.demo@laposta.local', 'BMW', 'M3', 2026, 4.5, 'Demo tags: M3 all-round speed', 'Rapidisimo, estable y usable con familia. La suspension firme puede cansar en calles malas.', 'Propietario actual', 2026, 12200, true, CURRENT_TIMESTAMP - INTERVAL '7 days', ARRAY['agile_engine','big_trunk','safe','uncomfortable']),
    ('santi.demo@laposta.local', 'BMW', 'M3', 2026, 4.0, 'Demo tags: M3 costly but capable', 'El mantenimiento premium se siente, pero tiene potencia y frenos para todo.', 'Propietario actual', 2026, 18500, true, CURRENT_TIMESTAMP - INTERVAL '11 days', ARRAY['expensive_maintenance','agile_engine','good_for_highway']),
    ('nadia.demo@laposta.local', 'BMW', 'M3', 2026, 4.0, 'Demo tags: M3 route weapon', 'En ruta es muy aplomado y seguro. En ciudad se nota ancho.', 'Ex propietario', 2025, 28000, true, CURRENT_TIMESTAMP - INTERVAL '15 days', ARRAY['good_for_highway','safe','hard_to_park']),

    ('driver.demo@laposta.local', 'Porsche', '911', 2026, 5.0, 'Demo tags: 911 benchmark steering', 'La direccion y el chasis son el punto de referencia. No intimida aunque vaya rapido.', 'Propietario actual', 2026, 6200, true, CURRENT_TIMESTAMP - INTERVAL '10 hours', ARRAY['agile_engine','good_for_highway','comfortable','good_resale']),
    ('eva.demo@laposta.local', 'Porsche', '911', 2026, 4.5, 'Demo tags: 911 daily usable', 'Sorprende lo facil que es usarlo todos los dias. El modo normal suaviza todo.', 'Propietario actual', 2026, 9400, true, CURRENT_TIMESTAMP - INTERVAL '30 hours', ARRAY['comfortable','good_for_city','agile_engine']),
    ('marco.demo@laposta.local', 'Porsche', '911', 2026, 4.0, 'Demo tags: 911 expensive magic', 'El costo de mantenimiento es alto, pero la experiencia y la reventa son fuertes.', 'Ex propietario', 2025, 18000, true, CURRENT_TIMESTAMP - INTERVAL '50 hours', ARRAY['expensive_maintenance','agile_engine','good_resale']),
    ('lucia.demo@laposta.local', 'Porsche', '911', 2026, 5.0, 'Demo tags: 911 safe pace', 'Da confianza en ruta mojada y los frenos aguantan uso intenso.', 'Propietario actual', 2026, 4100, true, CURRENT_TIMESTAMP - INTERVAL '70 hours', ARRAY['agile_engine','safe','good_for_highway']),
    ('santi.demo@laposta.local', 'Porsche', '911', 2026, 4.5, 'Demo tags: 911 quiet route', 'A velocidad constante es mas silencioso de lo esperado y los asientos no cansan.', 'Propietario actual', 2026, 12600, true, CURRENT_TIMESTAMP - INTERVAL '90 hours', ARRAY['comfortable','good_for_highway']),
    ('nadia.demo@laposta.local', 'Porsche', '911', 2026, 4.0, 'Demo tags: 911 symbolic rear seats', 'Adelante es perfecto, atras solo sirve para bolsos. El baul delantero ayuda poco.', 'Propietario actual', 2026, 7300, true, CURRENT_TIMESTAMP - INTERVAL '110 hours', ARRAY['small_trunk','agile_engine']),

    ('driver.demo@laposta.local', 'Honda', 'Civic Type R', 2026, 4.5, 'Demo tags: Type R practical track car', 'La caja manual es excelente y el motor empuja siempre. Tiene baul y plazas reales.', 'Propietario actual', 2026, 8100, true, CURRENT_TIMESTAMP - INTERVAL '13 days', ARRAY['agile_engine','big_trunk','good_for_highway']),
    ('eva.demo@laposta.local', 'Honda', 'Civic Type R', 2026, 4.0, 'Demo tags: Type R firm ride', 'Es practico para un deportivo, aunque el andar firme y el ruido se notan.', 'Propietario actual', 2026, 12800, true, CURRENT_TIMESTAMP - INTERVAL '16 days', ARRAY['big_trunk','noisy_cabin','uncomfortable','agile_engine']),
    ('marco.demo@laposta.local', 'Honda', 'Civic Type R', 2026, 4.5, 'Demo tags: Type R value', 'Ofrece rendimiento serio sin costos de mantenimiento de premium aleman.', 'Ex propietario', 2025, 24000, true, CURRENT_TIMESTAMP - INTERVAL '19 days', ARRAY['agile_engine','cheap_maintenance','good_resale']),

    ('lucia.demo@laposta.local', 'Subaru', 'WRX STI', 2026, 4.0, 'Demo tags: STI traction confidence', 'La traccion integral da mucha seguridad en lluvia y ripio. Manual muy mecanica.', 'Propietario actual', 2026, 15100, true, CURRENT_TIMESTAMP - INTERVAL '14 days', ARRAY['safe','agile_engine','good_for_highway']),
    ('santi.demo@laposta.local', 'Subaru', 'WRX STI', 2026, 3.5, 'Demo tags: STI thirsty', 'Consume bastante y el interior no es moderno, pero transmite mucha confianza.', 'Propietario actual', 2026, 22600, true, CURRENT_TIMESTAMP - INTERVAL '18 days', ARRAY['high_fuel_consumption','poor_tech','safe']),
    ('nadia.demo@laposta.local', 'Subaru', 'WRX STI', 2026, 4.0, 'Demo tags: STI owner cost', 'Los repuestos especificos no siempre son baratos, pero la comunidad ayuda mucho.', 'Ex propietario', 2025, 42000, true, CURRENT_TIMESTAMP - INTERVAL '22 days', ARRAY['hard_to_find_parts','agile_engine','good_resale']),

    ('driver.demo@laposta.local', 'Nissan', 'GT-R', 2026, 4.5, 'Demo tags: GT-R launch machine', 'Acelera con una violencia dificil de igualar y transmite mucha seguridad a alta velocidad.', 'Propietario actual', 2026, 7000, true, CURRENT_TIMESTAMP - INTERVAL '17 days', ARRAY['agile_engine','safe','good_for_highway']),
    ('eva.demo@laposta.local', 'Nissan', 'GT-R', 2026, 3.5, 'Demo tags: GT-R rough daily', 'Es rapido pero duro, ruidoso y caro de mantener si se usa seguido.', 'Ex propietario', 2025, 19000, true, CURRENT_TIMESTAMP - INTERVAL '20 days', ARRAY['agile_engine','noisy_cabin','expensive_maintenance','uncomfortable']),
    ('marco.demo@laposta.local', 'Nissan', 'GT-R', 2026, 4.0, 'Demo tags: GT-R supercar value', 'No es barato, pero entrega rendimiento de superdeportivo y mantiene interes usado.', 'Propietario actual', 2026, 13200, true, CURRENT_TIMESTAMP - INTERVAL '23 days', ARRAY['agile_engine','good_resale','high_fuel_consumption']),

    ('lucia.demo@laposta.local', 'Chevrolet', 'Camaro', 2026, 4.0, 'Demo tags: Camaro big power', 'El motor empuja muchisimo y el chasis sorprende. La visibilidad complica ciudad.', 'Propietario actual', 2026, 10400, true, CURRENT_TIMESTAMP - INTERVAL '21 days', ARRAY['agile_engine','hard_to_park','good_for_highway']),
    ('santi.demo@laposta.local', 'Chevrolet', 'Camaro', 2026, 3.5, 'Demo tags: Camaro thirsty', 'Divertido, potente y con mucho caracter. El consumo y los neumaticos pesan.', 'Ex propietario', 2025, 31000, true, CURRENT_TIMESTAMP - INTERVAL '24 days', ARRAY['high_fuel_consumption','expensive_maintenance','agile_engine']),
    ('nadia.demo@laposta.local', 'Chevrolet', 'Camaro', 2026, 4.0, 'Demo tags: Camaro weekend coupe', 'Para fines de semana es brillante. Para diario preferiria algo mas practico.', 'Propietario actual', 2026, 16600, true, CURRENT_TIMESTAMP - INTERVAL '27 days', ARRAY['agile_engine','small_trunk','bad_for_highway']),

    ('driver.demo@laposta.local', 'Audi', 'RS6 Avant', 2026, 4.5, 'Demo tags: RS6 family rocket', 'Tiene baul enorme, traccion integral y potencia absurda. Hace todo muy facil.', 'Propietario actual', 2026, 9100, true, CURRENT_TIMESTAMP - INTERVAL '25 days', ARRAY['big_trunk','agile_engine','safe','comfortable']),
    ('eva.demo@laposta.local', 'Audi', 'RS6 Avant', 2026, 4.0, 'Demo tags: RS6 expensive wagon', 'Comodisimo para ruta, pero mantenimiento y cubiertas son de auto premium pesado.', 'Propietario actual', 2026, 14000, true, CURRENT_TIMESTAMP - INTERVAL '28 days', ARRAY['comfortable','good_for_highway','expensive_maintenance','big_trunk']),
    ('marco.demo@laposta.local', 'Audi', 'RS6 Avant', 2026, 4.5, 'Demo tags: RS6 best one car', 'Si solo pudiera tener un auto rapido y familiar, seria este.', 'Ex propietario', 2025, 26000, true, CURRENT_TIMESTAMP - INTERVAL '31 days', ARRAY['big_trunk','good_for_highway','agile_engine','good_resale']);

INSERT INTO reviews (
    user_id, reviewer_email, car_id, rating, title, body, ownership_status,
    model_year, mileage_km, would_recommend, created_at, updated_at
)
SELECT
    u.user_id,
    NULL,
    c.car_id,
    d.rating,
    d.title,
    d.body,
    d.ownership_status,
    d.reviewed_model_year,
    d.mileage_km,
    d.would_recommend,
    d.created_at,
    d.created_at
FROM demo_review_seed d
JOIN users u ON u.email = d.reviewer_email
JOIN brands b ON b.name = d.brand_name
JOIN cars c ON c.brand_id = b.brand_id AND c.model = d.model AND c.year = d.car_year
WHERE NOT EXISTS (
    SELECT 1
    FROM reviews r
    WHERE r.user_id = u.user_id
      AND r.car_id = c.car_id
      AND r.title = d.title
);

INSERT INTO review_tag_assignments (review_id, tag_id, created_at)
SELECT
    r.review_id,
    rt.tag_id,
    CURRENT_TIMESTAMP
FROM demo_review_seed d
JOIN users u ON u.email = d.reviewer_email
JOIN brands b ON b.name = d.brand_name
JOIN cars c ON c.brand_id = b.brand_id AND c.model = d.model AND c.year = d.car_year
JOIN reviews r ON r.user_id = u.user_id AND r.car_id = c.car_id AND r.title = d.title
CROSS JOIN LATERAL unnest(d.tag_codes) AS tag_code(code)
JOIN review_tags rt ON rt.code = tag_code.code
ON CONFLICT (review_id, tag_id) DO NOTHING;

INSERT INTO review_replies (review_id, user_id, body, created_at, updated_at)
SELECT r.review_id, u.user_id, d.body, d.created_at, d.created_at
FROM (VALUES
    ('Demo tags: 911 benchmark steering', 'eva.demo@laposta.local', 'Esta resena deja muy claro por que el wizard deberia recomendarlo para performance.', CURRENT_TIMESTAMP - INTERVAL '9 hours'),
    ('Demo tags: Miata pure fun', 'marco.demo@laposta.local', 'Tambien suma mucho como primer deportivo manual.', CURRENT_TIMESTAMP - INTERVAL '3 days'),
    ('Demo tags: RS6 family rocket', 'lucia.demo@laposta.local', 'Buen ejemplo para probar preferencias de baul y ruta.', CURRENT_TIMESTAMP - INTERVAL '4 days')
) AS d(review_title, author_email, body, created_at)
JOIN reviews r ON r.title = d.review_title
JOIN users u ON u.email = d.author_email
WHERE NOT EXISTS (
    SELECT 1
    FROM review_replies rr
    WHERE rr.review_id = r.review_id
      AND rr.user_id = u.user_id
      AND rr.body = d.body
);

INSERT INTO review_likes (review_id, user_id, created_at)
SELECT r.review_id, u.user_id, CURRENT_TIMESTAMP
FROM reviews r
JOIN users u ON u.email IN ('driver.demo@laposta.local', 'eva.demo@laposta.local', 'marco.demo@laposta.local')
WHERE r.title IN (
    'Demo tags: 911 benchmark steering',
    'Demo tags: Miata pure fun',
    'Demo tags: RS6 family rocket',
    'Demo tags: Supra fast and focused',
    'Demo tags: Type R practical track car'
)
ON CONFLICT (review_id, user_id) DO NOTHING;

COMMIT;
