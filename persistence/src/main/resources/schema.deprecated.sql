-- Legacy/manual SQL extracted from schema.sql.
-- This file is intentionally NOT executed by application startup.
-- Execute only the relevant blocks in a controlled maintenance step.
-- Some blocks are one-shot legacy backfills; others load optional reference/demo data.
-- Several backfills assume the corresponding structural DDL already exists in the database.

BEGIN;

-- ============================================================
-- Legacy backfill: car_images image_id/display_order transition
-- ============================================================

UPDATE car_images
SET image_id = nextval('car_images_image_id_seq')
WHERE image_id IS NULL;

UPDATE car_images
SET display_order = 0
WHERE display_order IS NULL;

SELECT setval(
    'car_images_image_id_seq',
    COALESCE((SELECT MAX(image_id) FROM car_images), 1),
    (SELECT COUNT(*) > 0 FROM car_images)
);

-- ============================================================
-- Legacy backfill: copy inline request image payloads
-- ============================================================

INSERT INTO car_request_images (car_request_id, display_order, content_type, image_data, updated_at)
SELECT cr.car_request_id, 0, cr.image_content_type, cr.image_data, cr.created_at
FROM car_requests cr
WHERE cr.image_content_type IS NOT NULL
  AND cr.image_data IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM car_request_images cri
      WHERE cri.car_request_id = cr.car_request_id
  );

-- ============================================================
-- Legacy backfill: link rows by normalized email
-- ============================================================

UPDATE reviews r
SET user_id = u.user_id
FROM (
    SELECT LOWER(BTRIM(email)) AS email_key, MIN(user_id) AS user_id
    FROM users
    GROUP BY LOWER(BTRIM(email))
    HAVING COUNT(*) = 1
) u
WHERE r.user_id IS NULL
  AND r.reviewer_email IS NOT NULL
  AND LOWER(BTRIM(r.reviewer_email)) = u.email_key;

UPDATE car_requests cr
SET submitted_by_user_id = u.user_id
FROM (
    SELECT LOWER(BTRIM(email)) AS email_key, MIN(user_id) AS user_id
    FROM users
    GROUP BY LOWER(BTRIM(email))
    HAVING COUNT(*) = 1
) u
WHERE cr.submitted_by_user_id IS NULL
  AND cr.submitter_email IS NOT NULL
  AND LOWER(BTRIM(cr.submitter_email)) = u.email_key;

-- ============================================================
-- Legacy backfill: review ownership status labels
-- ============================================================

UPDATE reviews
SET ownership_status = 'current_owner'
WHERE ownership_status = 'Propietario actual';

UPDATE reviews
SET ownership_status = 'previous_owner'
WHERE ownership_status = 'Ex propietario';

-- ============================================================
-- Optional reference/demo seed data
-- ============================================================

INSERT INTO brands (name) VALUES
    ('Toyota'), ('Ford'), ('Mazda'), ('BMW'), ('Porsche'),
    ('Honda'), ('Subaru'), ('Nissan'), ('Chevrolet'), ('Audi')
ON CONFLICT (name) DO NOTHING;

INSERT INTO body_types (name) VALUES
    ('Coupe'), ('Sedan'), ('Roadster'), ('Hatchback'), ('Estate')
ON CONFLICT (name) DO NOTHING;

INSERT INTO cars (brand_id, model, body_type_id, year, description)
SELECT b.brand_id, 'GR Supra', bt.body_type_id, 2026,
       'La Supra de quinta generación, codesarrollada con BMW bajo la submarca GR, revivió el icónico nombre en 2019 tras 17 años de ausencia. Está propulsada por un turbo de seis cilindros en línea compartido con el BMW Z4 G29.'
FROM brands b, body_types bt WHERE b.name = 'Toyota' AND bt.name = 'Coupe'
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars (brand_id, model, body_type_id, year, description)
SELECT b.brand_id, 'Mustang', bt.body_type_id, 2026,
       'El Mustang de sexta generación fue el primero en ofrecer suspensión trasera independiente a nivel mundial e introdujo la opción de cuatro cilindros turbo EcoBoost junto a su icónico V8.'
FROM brands b, body_types bt WHERE b.name = 'Ford' AND bt.name = 'Coupe'
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars (brand_id, model, body_type_id, year, description)
SELECT b.brand_id, 'MX-5 Miata', bt.body_type_id, 2026,
       'El MX-5 de cuarta generación redujo más de 100 kg respecto a su predecesor, volviendo a la estrategia original de ingeniería ligera. Sigue siendo el roadster de dos plazas más vendido del mundo.'
FROM brands b, body_types bt WHERE b.name = 'Mazda' AND bt.name = 'Roadster'
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars (brand_id, model, body_type_id, year, description)
SELECT b.brand_id, 'M3', bt.body_type_id, 2026,
       'El G80 M3 está propulsado por el S58 de seis cilindros en línea biturbo que desarrolla hasta 503 hp en versión Competition. Fue el primer M3 con tracción total opcional y el primero en dar origen a una variante M3 Touring.'
FROM brands b, body_types bt WHERE b.name = 'BMW' AND bt.name = 'Sedan'
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars (brand_id, model, body_type_id, year, description)
SELECT b.brand_id, '911', bt.body_type_id, 2026,
       'El 911 de generación 992 es 45 mm más ancho que su predecesor y adopta una carrocería íntegramente de aluminio. Conserva el icónico motor bóxer de seis cilindros trasero e incorpora manijas eléctricas retráctiles.'
FROM brands b, body_types bt WHERE b.name = 'Porsche' AND bt.name = 'Coupe'
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars (brand_id, model, body_type_id, year, description)
SELECT b.brand_id, 'Civic Type R', bt.body_type_id, 2026,
       'El FL5 Type R equipa el motor turbo K20C1 de 330 PS e incorpora paneles traseros más anchos. Incluye el registrador de datos LogR de Honda para uso en pista y un diseño más depurado respecto al FK8.'
FROM brands b, body_types bt WHERE b.name = 'Honda' AND bt.name = 'Hatchback'
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars (brand_id, model, body_type_id, year, description)
SELECT b.brand_id, 'WRX STI', bt.body_type_id, 2026,
       'El WRX STI de generación VA fue el primero en ofrecerse únicamente como sedán, con el motor bóxer de cuatro cilindros turbo EJ257 de 305 hp, caja de 6 velocidades manual y tracción total simétrica.'
FROM brands b, body_types bt WHERE b.name = 'Subaru' AND bt.name = 'Sedan'
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars (brand_id, model, body_type_id, year, description)
SELECT b.brand_id, 'GT-R', bt.body_type_id, 2026,
       'El R35 GT-R se construyó en torno al VR38DETT biturbo V6 ensamblado a mano, con cada motor firmado por uno de los nueve artesanos Takumi certificados. Su combinación de tracción total y control de largada lo convirtió en un referente destructor de supercars.'
FROM brands b, body_types bt WHERE b.name = 'Nissan' AND bt.name = 'Coupe'
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars (brand_id, model, body_type_id, year, description)
SELECT b.brand_id, 'Camaro', bt.body_type_id, 2026,
       'Construido sobre la plataforma Alpha de GM compartida con el Cadillac ATS, el Camaro de sexta generación redujo más de 90 kg respecto a su predecesor. Los motores disponibles van desde un cuatro cilindros turbo de 2.0L hasta el V8 sobrealimentado de 6.2L del ZL1.'
FROM brands b, body_types bt WHERE b.name = 'Chevrolet' AND bt.name = 'Coupe'
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars (brand_id, model, body_type_id, year, description)
SELECT b.brand_id, 'RS6 Avant', bt.body_type_id, 2026,
       'El C8 RS6 Avant está propulsado por un V8 biturbo de 4.0L con sistema mild-hybrid de 48V que desarrolla 600 PS. Fue el primer RS6 Avant vendido en Norteamérica desde la generación C5 y el primero en usar una transmisión híbrida.'
FROM brands b, body_types bt WHERE b.name = 'Audi' AND bt.name = 'Estate'
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

UPDATE cars SET fuel_type='combustion', horsepower=382, airbag_count=8,  transmission='automatic', fuel_consumption=9.8,  max_speed_kmh=250, price_usd=51290, year=2026
WHERE car_id=(SELECT c.car_id FROM cars c JOIN brands b ON c.brand_id=b.brand_id JOIN body_types bt ON c.body_type_id=bt.body_type_id WHERE b.name='Toyota'    AND c.model='GR Supra' AND bt.name='Coupe' AND c.year=2026);

UPDATE cars SET fuel_type='combustion', horsepower=450, airbag_count=6,  transmission='automatic', fuel_consumption=12.4, max_speed_kmh=250, price_usd=42995, year=2026
WHERE car_id=(SELECT c.car_id FROM cars c JOIN brands b ON c.brand_id=b.brand_id JOIN body_types bt ON c.body_type_id=bt.body_type_id WHERE b.name='Ford'      AND c.model='Mustang' AND bt.name='Coupe' AND c.year=2026);

UPDATE cars SET fuel_type='combustion', horsepower=184, airbag_count=6,  transmission='manual',    fuel_consumption=7.4,  max_speed_kmh=214, price_usd=28050, year=2026
WHERE car_id=(SELECT c.car_id FROM cars c JOIN brands b ON c.brand_id=b.brand_id JOIN body_types bt ON c.body_type_id=bt.body_type_id WHERE b.name='Mazda'     AND c.model='MX-5 Miata' AND bt.name='Roadster' AND c.year=2026);

UPDATE cars SET fuel_type='combustion', horsepower=503, airbag_count=10, transmission='automatic', fuel_consumption=10.5, max_speed_kmh=290, price_usd=76900, year=2026
WHERE car_id=(SELECT c.car_id FROM cars c JOIN brands b ON c.brand_id=b.brand_id JOIN body_types bt ON c.body_type_id=bt.body_type_id WHERE b.name='BMW'       AND c.model='M3' AND bt.name='Sedan' AND c.year=2026);

UPDATE cars SET fuel_type='combustion', horsepower=385, airbag_count=8,  transmission='automatic', fuel_consumption=10.2, max_speed_kmh=293, price_usd=106100, year=2026
WHERE car_id=(SELECT c.car_id FROM cars c JOIN brands b ON c.brand_id=b.brand_id JOIN body_types bt ON c.body_type_id=bt.body_type_id WHERE b.name='Porsche'   AND c.model='911' AND bt.name='Coupe' AND c.year=2026);

UPDATE cars SET fuel_type='combustion', horsepower=330, airbag_count=6,  transmission='manual',    fuel_consumption=8.9,  max_speed_kmh=272, price_usd=42895, year=2026
WHERE car_id=(SELECT c.car_id FROM cars c JOIN brands b ON c.brand_id=b.brand_id JOIN body_types bt ON c.body_type_id=bt.body_type_id WHERE b.name='Honda'     AND c.model='Civic Type R' AND bt.name='Hatchback' AND c.year=2026);

UPDATE cars SET fuel_type='combustion', horsepower=305, airbag_count=6,  transmission='manual',    fuel_consumption=10.7, max_speed_kmh=255, price_usd=39995, year=2026
WHERE car_id=(SELECT c.car_id FROM cars c JOIN brands b ON c.brand_id=b.brand_id JOIN body_types bt ON c.body_type_id=bt.body_type_id WHERE b.name='Subaru'    AND c.model='WRX STI' AND bt.name='Sedan' AND c.year=2026);

UPDATE cars SET fuel_type='combustion', horsepower=570, airbag_count=6,  transmission='automatic', fuel_consumption=12.4, max_speed_kmh=315, price_usd=113540, year=2026
WHERE car_id=(SELECT c.car_id FROM cars c JOIN brands b ON c.brand_id=b.brand_id JOIN body_types bt ON c.body_type_id=bt.body_type_id WHERE b.name='Nissan'    AND c.model='GT-R' AND bt.name='Coupe' AND c.year=2026);

UPDATE cars SET fuel_type='combustion', horsepower=650, airbag_count=6,  transmission='automatic', fuel_consumption=14.7, max_speed_kmh=290, price_usd=62995, year=2026
WHERE car_id=(SELECT c.car_id FROM cars c JOIN brands b ON c.brand_id=b.brand_id JOIN body_types bt ON c.body_type_id=bt.body_type_id WHERE b.name='Chevrolet' AND c.model='Camaro' AND bt.name='Coupe' AND c.year=2026);

UPDATE cars SET fuel_type='hybrid',     horsepower=600, airbag_count=10, transmission='automatic', fuel_consumption=11.5, max_speed_kmh=280, price_usd=118600, year=2026
WHERE car_id=(SELECT c.car_id FROM cars c JOIN brands b ON c.brand_id=b.brand_id JOIN body_types bt ON c.body_type_id=bt.body_type_id WHERE b.name='Audi'      AND c.model='RS6 Avant' AND bt.name='Estate' AND c.year=2026);

INSERT INTO review_tags (code, label_es, sentiment, dimension) VALUES
    ('low_fuel_consumption',  'Consume poco',           'positive', 'fuel_consumption'),
    ('high_fuel_consumption', 'Consume mucho',          'negative', 'fuel_consumption'),
    ('comfortable',           'Cómodo',                 'positive', 'comfort'),
    ('uncomfortable',         'Incómodo',               'negative', 'comfort'),
    ('cheap_maintenance',     'Barato de mantener',     'positive', 'maintenance_cost'),
    ('expensive_maintenance', 'Mantenimiento caro',     'negative', 'maintenance_cost'),
    ('hard_to_find_parts',    'Repuestos caros/difíciles', 'negative', 'parts_availability'),
    ('safe',                  'Seguro',                 'positive', 'safety'),
    ('big_trunk',             'Buen baúl',              'positive', 'trunk_size'),
    ('small_trunk',           'Baúl chico',             'negative', 'trunk_size'),
    ('good_for_city',         'Bueno para ciudad',      'positive', 'city'),
    ('good_for_highway',      'Bueno para ruta',        'positive', 'highway'),
    ('bad_for_highway',       'Malo para ruta',         'negative', 'highway'),
    ('good_first_car',        'Buen primer auto',       'positive', 'first_car'),
    ('good_resale',           'Buena reventa',          'positive', 'resale'),
    ('bad_resale',            'Mala reventa',           'negative', 'resale'),
    ('agile_engine',          'Motor ágil',             'positive', 'agility'),
    ('underpowered',          'Le falta potencia',      'negative', 'agility'),
    ('easy_to_park',          'Fácil de estacionar',    'positive', 'parking'),
    ('hard_to_park',          'Difícil de estacionar',  'negative', 'parking'),
    ('noisy_cabin',           'Mucho ruido interior',   'negative', 'cabin_noise'),
    ('poor_tech',             'Tecnología pobre',       'negative', 'tech')
ON CONFLICT (code) DO NOTHING;

-- ============================================================
-- Legacy backfill: community search and topic data
-- ============================================================

UPDATE communities
SET search_vector = communities_build_search_vector(name, slug, description)
WHERE search_vector IS NULL;

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
ON CONFLICT ((LOWER(BTRIM(code)))) DO NOTHING;

UPDATE community_topic_assignments
SET topic_id = (SELECT topic_id FROM community_topics WHERE code = 'mechanical')
WHERE topic_id IN (SELECT topic_id FROM community_topics WHERE code = 'repairs');

UPDATE community_topic_assignments
SET topic_id = (SELECT topic_id FROM community_topics WHERE code = 'marketplace')
WHERE topic_id IN (SELECT topic_id FROM community_topics WHERE code = 'buying');

UPDATE community_topics
SET active = FALSE
WHERE code IN ('brands', 'repairs', 'reviews', 'buying', 'local', 'daily');

COMMIT;
