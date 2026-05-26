-- =============================================================================
-- Demo: reviews + chips (review_tag_assignments) para el recomendador.
-- Requisito: base ya inicializada con schema.sql (marcas, autos 2026, review_tags).
--
-- Uso (ejemplo):
--   psql "$DB_URL" -v ON_ERROR_STOP=1 -f persistence/src/main/resources/demo-recommendation-reviews-seed.sql
--
-- Idempotencia: borra solo reviews con reviewer_email = 'demo.seed.rec@itba.paw'
-- y vuelve a insertar.
-- =============================================================================

DELETE FROM reviews WHERE reviewer_email = 'demo.seed.rec@itba.paw';

-- ---------------------------------------------------------------------------
-- Mazda MX-5 Miata: ciudad, primer auto, fácil de estacionar, poco consumo
-- ---------------------------------------------------------------------------
WITH inserted AS (
    INSERT INTO reviews (reviewer_email, car_id, rating, title, body, created_at, updated_at)
    SELECT 'demo.seed.rec@itba.paw',
           c.car_id,
           4.2,
           'Demo rec MX-5 #' || g.n,
           'Reseña sintética para demo del recomendador (ciudad / primer auto).',
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
    FROM cars c
             JOIN brands b ON b.brand_id = c.brand_id
             JOIN body_types bt ON bt.body_type_id = c.body_type_id
             CROSS JOIN generate_series(1, 14) AS g(n)
    WHERE b.name = 'Mazda'
      AND c.model = 'MX-5 Miata'
      AND bt.name = 'Roadster'
      AND c.year = 2026
    RETURNING review_id
)
INSERT
INTO review_tag_assignments (review_id, tag_id)
SELECT i.review_id,
       t.tag_id
FROM inserted i
         CROSS JOIN review_tags t
WHERE t.code IN (
                 'good_for_city',
                 'easy_to_park',
                 'good_first_car',
                 'safe',
                 'low_fuel_consumption',
                 'comfortable'
    );

-- ---------------------------------------------------------------------------
-- Audi RS6 Avant: ruta, baúl grande, confort, algo de agilidad
-- ---------------------------------------------------------------------------
WITH inserted AS (
    INSERT INTO reviews (reviewer_email, car_id, rating, title, body, created_at, updated_at)
    SELECT 'demo.seed.rec@itba.paw',
           c.car_id,
           4.6,
           'Demo rec RS6 #' || g.n,
           'Reseña sintética para demo (ruta + baúl + confort).',
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
    FROM cars c
             JOIN brands b ON b.brand_id = c.brand_id
             JOIN body_types bt ON bt.body_type_id = c.body_type_id
             CROSS JOIN generate_series(1, 12) AS g(n)
    WHERE b.name = 'Audi'
      AND c.model = 'RS6 Avant'
      AND bt.name = 'Estate'
      AND c.year = 2026
    RETURNING review_id
)
INSERT
INTO review_tag_assignments (review_id, tag_id)
SELECT i.review_id,
       t.tag_id
FROM inserted i
         CROSS JOIN review_tags t
WHERE t.code IN (
                 'good_for_highway',
                 'big_trunk',
                 'comfortable',
                 'agile_engine',
                 'safe',
                 'low_fuel_consumption'
    );

-- ---------------------------------------------------------------------------
-- Nissan GT-R: autopista + motor ágil (gana si priorizás performance+ruta)
-- ---------------------------------------------------------------------------
WITH inserted AS (
    INSERT INTO reviews (reviewer_email, car_id, rating, title, body, created_at, updated_at)
    SELECT 'demo.seed.rec@itba.paw',
           c.car_id,
           4.8,
           'Demo rec GT-R #' || g.n,
           'Reseña sintética para demo (ruta + performance).',
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
    FROM cars c
             JOIN brands b ON b.brand_id = c.brand_id
             JOIN body_types bt ON bt.body_type_id = c.body_type_id
             CROSS JOIN generate_series(1, 16) AS g(n)
    WHERE b.name = 'Nissan'
      AND c.model = 'GT-R'
      AND bt.name = 'Coupe'
      AND c.year = 2026
    RETURNING review_id
)
INSERT
INTO review_tag_assignments (review_id, tag_id)
SELECT i.review_id,
       t.tag_id
FROM inserted i
         CROSS JOIN review_tags t
WHERE t.code IN (
                 'good_for_highway',
                 'agile_engine',
                 'comfortable'
    );

-- ---------------------------------------------------------------------------
-- Porsche 911: también ruta + ágil, menos reviews que el GT-R (desempate)
-- ---------------------------------------------------------------------------
WITH inserted AS (
    INSERT INTO reviews (reviewer_email, car_id, rating, title, body, created_at, updated_at)
    SELECT 'demo.seed.rec@itba.paw',
           c.car_id,
           4.9,
           'Demo rec 911 #' || g.n,
           'Reseña sintética para demo (ruta + performance).',
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
    FROM cars c
             JOIN brands b ON b.brand_id = c.brand_id
             JOIN body_types bt ON bt.body_type_id = c.body_type_id
             CROSS JOIN generate_series(1, 10) AS g(n)
    WHERE b.name = 'Porsche'
      AND c.model = '911'
      AND bt.name = 'Coupe'
      AND c.year = 2026
    RETURNING review_id
)
INSERT
INTO review_tag_assignments (review_id, tag_id)
SELECT i.review_id,
       t.tag_id
FROM inserted i
         CROSS JOIN review_tags t
WHERE t.code IN (
                 'good_for_highway',
                 'agile_engine',
                 'good_for_city'
    );

-- ---------------------------------------------------------------------------
-- Honda Civic Type R: ciudad + bajo consumo (sin foco “primer auto”)
-- ---------------------------------------------------------------------------
WITH inserted AS (
    INSERT INTO reviews (reviewer_email, car_id, rating, title, body, created_at, updated_at)
    SELECT 'demo.seed.rec@itba.paw',
           c.car_id,
           4.4,
           'Demo rec CTR #' || g.n,
           'Reseña sintética para demo (ciudad + consumo).',
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
    FROM cars c
             JOIN brands b ON b.brand_id = c.brand_id
             JOIN body_types bt ON bt.body_type_id = c.body_type_id
             CROSS JOIN generate_series(1, 18) AS g(n)
    WHERE b.name = 'Honda'
      AND c.model = 'Civic Type R'
      AND bt.name = 'Hatchback'
      AND c.year = 2026
    RETURNING review_id
)
INSERT
INTO review_tag_assignments (review_id, tag_id)
SELECT i.review_id,
       t.tag_id
FROM inserted i
         CROSS JOIN review_tags t
WHERE t.code IN (
                 'good_for_city',
                 'low_fuel_consumption',
                 'comfortable',
                 'agile_engine',
                 'easy_to_park'
    );

-- ---------------------------------------------------------------------------
-- Chevrolet Camaro: mucho consumo / difícil de estacionar (contraste ciudad)
-- ---------------------------------------------------------------------------
WITH inserted AS (
    INSERT INTO reviews (reviewer_email, car_id, rating, title, body, created_at, updated_at)
    SELECT 'demo.seed.rec@itba.paw',
           c.car_id,
           4.0,
           'Demo rec Camaro #' || g.n,
           'Reseña sintética para demo (muscle / alto consumo).',
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
    FROM cars c
             JOIN brands b ON b.brand_id = c.brand_id
             JOIN body_types bt ON bt.body_type_id = c.body_type_id
             CROSS JOIN generate_series(1, 11) AS g(n)
    WHERE b.name = 'Chevrolet'
      AND c.model = 'Camaro'
      AND bt.name = 'Coupe'
      AND c.year = 2026
    RETURNING review_id
)
INSERT
INTO review_tag_assignments (review_id, tag_id)
SELECT i.review_id,
       t.tag_id
FROM inserted i
         CROSS JOIN review_tags t
WHERE t.code IN (
                 'high_fuel_consumption',
                 'agile_engine',
                 'good_for_highway',
                 'hard_to_park',
                 'small_trunk',
                 'noisy_cabin'
    );

-- ---------------------------------------------------------------------------
-- Ford Mustang: malo para ruta (penaliza respuesta “autopista”)
-- ---------------------------------------------------------------------------
WITH inserted AS (
    INSERT INTO reviews (reviewer_email, car_id, rating, title, body, created_at, updated_at)
    SELECT 'demo.seed.rec@itba.paw',
           c.car_id,
           3.9,
           'Demo rec Mustang #' || g.n,
           'Reseña sintética para demo (bad_for_highway).',
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
    FROM cars c
             JOIN brands b ON b.brand_id = c.brand_id
             JOIN body_types bt ON bt.body_type_id = c.body_type_id
             CROSS JOIN generate_series(1, 9) AS g(n)
    WHERE b.name = 'Ford'
      AND c.model = 'Mustang'
      AND bt.name = 'Coupe'
      AND c.year = 2026
    RETURNING review_id
)
INSERT
INTO review_tag_assignments (review_id, tag_id)
SELECT i.review_id,
       t.tag_id
FROM inserted i
         CROSS JOIN review_tags t
WHERE t.code IN (
                 'bad_for_highway',
                 'noisy_cabin',
                 'hard_to_park',
                 'agile_engine'
    );

-- ---------------------------------------------------------------------------
-- Toyota GR Supra: mix autopista + ágil (intermedio)
-- ---------------------------------------------------------------------------
WITH inserted AS (
    INSERT INTO reviews (reviewer_email, car_id, rating, title, body, created_at, updated_at)
    SELECT 'demo.seed.rec@itba.paw',
           c.car_id,
           4.3,
           'Demo rec Supra #' || g.n,
           'Reseña sintética para demo (mix ruta).',
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
    FROM cars c
             JOIN brands b ON b.brand_id = c.brand_id
             JOIN body_types bt ON bt.body_type_id = c.body_type_id
             CROSS JOIN generate_series(1, 9) AS g(n)
    WHERE b.name = 'Toyota'
      AND c.model = 'GR Supra'
      AND bt.name = 'Coupe'
      AND c.year = 2026
    RETURNING review_id
)
INSERT
INTO review_tag_assignments (review_id, tag_id)
SELECT i.review_id,
       t.tag_id
FROM inserted i
         CROSS JOIN review_tags t
WHERE t.code IN (
                 'good_for_highway',
                 'agile_engine',
                 'comfortable'
    );

-- ---------------------------------------------------------------------------
-- BMW M3: ciudad + performance (alternativa al GT-R si filtrás sedán — opcional)
-- ---------------------------------------------------------------------------
WITH inserted AS (
    INSERT INTO reviews (reviewer_email, car_id, rating, title, body, created_at, updated_at)
    SELECT 'demo.seed.rec@itba.paw',
           c.car_id,
           4.5,
           'Demo rec M3 #' || g.n,
           'Reseña sintética para demo (sedán deportivo).',
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
    FROM cars c
             JOIN brands b ON b.brand_id = c.brand_id
             JOIN body_types bt ON bt.body_type_id = c.body_type_id
             CROSS JOIN generate_series(1, 8) AS g(n)
    WHERE b.name = 'BMW'
      AND c.model = 'M3'
      AND bt.name = 'Sedan'
      AND c.year = 2026
    RETURNING review_id
)
INSERT
INTO review_tag_assignments (review_id, tag_id)
SELECT i.review_id,
       t.tag_id
FROM inserted i
         CROSS JOIN review_tags t
WHERE t.code IN (
                 'good_for_city',
                 'agile_engine',
                 'comfortable',
                 'high_fuel_consumption'
    );

-- ---------------------------------------------------------------------------
-- Subaru WRX STI: mix (no debería ganar ningún guion fuerte; relleno realista)
-- ---------------------------------------------------------------------------
WITH inserted AS (
    INSERT INTO reviews (reviewer_email, car_id, rating, title, body, created_at, updated_at)
    SELECT 'demo.seed.rec@itba.paw',
           c.car_id,
           4.1,
           'Demo rec WRX #' || g.n,
           'Reseña sintética para demo (mix general).',
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
    FROM cars c
             JOIN brands b ON b.brand_id = c.brand_id
             JOIN body_types bt ON bt.body_type_id = c.body_type_id
             CROSS JOIN generate_series(1, 7) AS g(n)
    WHERE b.name = 'Subaru'
      AND c.model = 'WRX STI'
      AND bt.name = 'Sedan'
      AND c.year = 2026
    RETURNING review_id
)
INSERT
INTO review_tag_assignments (review_id, tag_id)
SELECT i.review_id,
       t.tag_id
FROM inserted i
         CROSS JOIN review_tags t
WHERE t.code IN (
                 'good_for_highway',
                 'agile_engine',
                 'uncomfortable',
                 'high_fuel_consumption'
    );
