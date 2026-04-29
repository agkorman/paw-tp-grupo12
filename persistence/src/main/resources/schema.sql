-- ============================================================
-- Tables
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    user_id    SERIAL       PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL DEFAULT 'user',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE users
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

CREATE TABLE IF NOT EXISTS user_follows (
    follower_id INT         NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    followed_id INT         NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_user_follows PRIMARY KEY (follower_id, followed_id),
    CONSTRAINT chk_user_follows_no_self_follow CHECK (follower_id <> followed_id)
);

CREATE INDEX IF NOT EXISTS idx_user_follows_followed_id ON user_follows (followed_id);
CREATE INDEX IF NOT EXISTS idx_user_follows_follower_id ON user_follows (follower_id);

CREATE TABLE IF NOT EXISTS brands (
    brand_id   BIGSERIAL   PRIMARY KEY,
    name       VARCHAR(80)  NOT NULL UNIQUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS body_types (
    body_type_id SMALLSERIAL PRIMARY KEY,
    name         VARCHAR(50)  NOT NULL UNIQUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cars (
    car_id         BIGSERIAL    PRIMARY KEY,
    brand_id       BIGINT       NOT NULL REFERENCES brands(brand_id)         ON DELETE RESTRICT,
    model          VARCHAR(120) NOT NULL,
    body_type_id   SMALLINT     NOT NULL REFERENCES body_types(body_type_id) ON DELETE RESTRICT,
    year           INT,
    description    TEXT,
    search_vector  tsvector,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_cars_brand_model_body_type_year UNIQUE NULLS NOT DISTINCT (brand_id, model, body_type_id, year)
);

CREATE INDEX IF NOT EXISTS idx_cars_brand_id        ON cars   (brand_id);
CREATE INDEX IF NOT EXISTS idx_cars_body_type_id    ON cars   (body_type_id);
CREATE INDEX IF NOT EXISTS idx_cars_brand_body_type ON cars   (brand_id, body_type_id);
CREATE INDEX IF NOT EXISTS idx_cars_fts             ON cars   USING GIN (search_vector);

CREATE TABLE IF NOT EXISTS car_favorites (
    user_id    INT         NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    car_id     BIGINT      NOT NULL REFERENCES cars(car_id)   ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_car_favorites PRIMARY KEY (user_id, car_id)
);

CREATE INDEX IF NOT EXISTS idx_car_favorites_car_id ON car_favorites (car_id);
CREATE INDEX IF NOT EXISTS idx_car_favorites_user_created_at ON car_favorites (user_id, created_at DESC);

CREATE OR REPLACE FUNCTION cars_build_search_vector(
    p_brand_name  TEXT,
    p_model       TEXT,
    p_body_type   TEXT,
    p_description TEXT
) RETURNS tsvector LANGUAGE sql IMMUTABLE AS $$
    SELECT
        setweight(to_tsvector('simple', COALESCE(p_brand_name,  '')), 'A') ||
        setweight(to_tsvector('simple', COALESCE(p_model,       '')), 'A') ||
        setweight(to_tsvector('simple', COALESCE(p_body_type,   '')), 'B') ||
        setweight(to_tsvector('simple', COALESCE(p_description, '')), 'C')
$$;

CREATE OR REPLACE FUNCTION cars_search_vector_trigger_fn()
RETURNS TRIGGER LANGUAGE plpgsql AS
'DECLARE
    v_brand_name TEXT;
    v_body_type  TEXT;
BEGIN
    SELECT name INTO v_brand_name FROM brands     WHERE brand_id     = NEW.brand_id;
    SELECT name INTO v_body_type  FROM body_types WHERE body_type_id = NEW.body_type_id;
    NEW.search_vector := cars_build_search_vector(v_brand_name, NEW.model, v_body_type, NEW.description);
    RETURN NEW;
END';

CREATE OR REPLACE TRIGGER cars_search_vector_trigger
    BEFORE INSERT OR UPDATE ON cars
    FOR EACH ROW EXECUTE FUNCTION cars_search_vector_trigger_fn();

CREATE TABLE IF NOT EXISTS car_requests (
    car_request_id       BIGSERIAL    PRIMARY KEY,
    submitted_by_user_id INT          REFERENCES users(user_id)             ON DELETE SET NULL,
    submitter_email      VARCHAR(100),
    brand_id             BIGINT       NOT NULL REFERENCES brands(brand_id)         ON DELETE RESTRICT,
    body_type_id         SMALLINT     NOT NULL REFERENCES body_types(body_type_id) ON DELETE RESTRICT,
    year                 INT,
    model                VARCHAR(120) NOT NULL,
    description          TEXT         NOT NULL,
    image_content_type   VARCHAR(100),
    image_data           BYTEA,
    status               VARCHAR(20)  NOT NULL DEFAULT 'pending',
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_car_requests_submitter_identity
        CHECK (submitted_by_user_id IS NOT NULL OR submitter_email IS NOT NULL),
    CONSTRAINT chk_car_requests_status
        CHECK (status IN ('pending', 'approved', 'rejected')),
    CONSTRAINT chk_car_requests_image_payload
        CHECK (
            (image_content_type IS NULL AND image_data IS NULL)
            OR (image_content_type IS NOT NULL AND image_data IS NOT NULL)
        )
);

ALTER TABLE car_requests
    ADD COLUMN IF NOT EXISTS submitted_by_user_id INT REFERENCES users(user_id) ON DELETE SET NULL;

ALTER TABLE car_requests
    ADD COLUMN IF NOT EXISTS submitter_email VARCHAR(100);

ALTER TABLE car_requests
    ALTER COLUMN submitter_email DROP NOT NULL;

CREATE SEQUENCE IF NOT EXISTS car_images_image_id_seq;

CREATE TABLE IF NOT EXISTS car_images (
    image_id      BIGINT       PRIMARY KEY DEFAULT nextval('car_images_image_id_seq'),
    car_id        BIGINT       NOT NULL REFERENCES cars(car_id) ON DELETE CASCADE,
    display_order INT          NOT NULL DEFAULT 0,
    content_type  VARCHAR(100) NOT NULL,
    image_data    BYTEA        NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE car_images
    ADD COLUMN IF NOT EXISTS image_id BIGINT;

ALTER TABLE car_images
    ADD COLUMN IF NOT EXISTS display_order INT;

ALTER TABLE car_images
    ALTER COLUMN image_id SET DEFAULT nextval('car_images_image_id_seq');

UPDATE car_images
SET image_id = nextval('car_images_image_id_seq')
WHERE image_id IS NULL;

UPDATE car_images
SET display_order = 0
WHERE display_order IS NULL;

ALTER TABLE car_images
    ALTER COLUMN image_id SET NOT NULL;

ALTER TABLE car_images
    ALTER COLUMN display_order SET DEFAULT 0;

ALTER TABLE car_images
    ALTER COLUMN display_order SET NOT NULL;

ALTER SEQUENCE car_images_image_id_seq OWNED BY car_images.image_id;

SELECT setval(
    'car_images_image_id_seq',
    COALESCE((SELECT MAX(image_id) FROM car_images), 1),
    (SELECT COUNT(*) > 0 FROM car_images)
);

ALTER TABLE car_images
    DROP CONSTRAINT IF EXISTS car_images_pkey;

ALTER TABLE car_images
    DROP CONSTRAINT IF EXISTS pk_car_images;

ALTER TABLE car_images
    ADD CONSTRAINT pk_car_images PRIMARY KEY (image_id);

ALTER TABLE car_images
    DROP CONSTRAINT IF EXISTS chk_car_images_display_order;

ALTER TABLE car_images
    ADD CONSTRAINT chk_car_images_display_order CHECK (display_order >= 0);

CREATE INDEX IF NOT EXISTS idx_car_images_car_id ON car_images (car_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_car_images_car_order ON car_images (car_id, display_order);

CREATE TABLE IF NOT EXISTS car_request_images (
    image_id       BIGSERIAL    PRIMARY KEY,
    car_request_id BIGINT       NOT NULL REFERENCES car_requests(car_request_id) ON DELETE CASCADE,
    display_order  INT          NOT NULL DEFAULT 0,
    content_type   VARCHAR(100) NOT NULL,
    image_data     BYTEA        NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_car_request_images_display_order CHECK (display_order >= 0)
);

CREATE INDEX IF NOT EXISTS idx_car_request_images_request_id ON car_request_images (car_request_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_car_request_images_request_order
    ON car_request_images (car_request_id, display_order);

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

CREATE TABLE IF NOT EXISTS reviews (
    review_id        SERIAL       PRIMARY KEY,
    user_id          INT          REFERENCES users(user_id),
    reviewer_email   VARCHAR(100),
    car_id           BIGINT       NOT NULL REFERENCES cars(car_id),
    rating           NUMERIC(3,1) NOT NULL CHECK (rating BETWEEN 0.0 AND 5.0),
    title            VARCHAR(200) NOT NULL,
    body             TEXT         NOT NULL,
    ownership_status VARCHAR(20),
    model_year       INT,
    mileage_km       INT,
    would_recommend  BOOLEAN,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_reviews_reviewer_identity
        CHECK (user_id IS NOT NULL OR reviewer_email IS NOT NULL)
);

ALTER TABLE reviews
    ADD COLUMN IF NOT EXISTS user_id INT REFERENCES users(user_id);

ALTER TABLE reviews
    ADD COLUMN IF NOT EXISTS reviewer_email VARCHAR(100);

ALTER TABLE reviews
    ALTER COLUMN reviewer_email DROP NOT NULL;

CREATE INDEX IF NOT EXISTS idx_reviews_car_id ON reviews (car_id);
CREATE INDEX IF NOT EXISTS idx_reviews_user_id ON reviews (user_id);

CREATE TABLE IF NOT EXISTS review_replies (
    reply_id   SERIAL      PRIMARY KEY,
    review_id  INT         NOT NULL REFERENCES reviews(review_id) ON DELETE CASCADE,
    user_id    INT         NOT NULL REFERENCES users(user_id),
    body       TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_review_replies_body_not_blank CHECK (BTRIM(body) <> '')
);

CREATE INDEX IF NOT EXISTS idx_review_replies_review_id_created_at
    ON review_replies (review_id, created_at ASC, reply_id ASC);
CREATE INDEX IF NOT EXISTS idx_review_replies_user_id ON review_replies (user_id);

CREATE TABLE IF NOT EXISTS review_likes (
    review_id  INT         NOT NULL REFERENCES reviews(review_id) ON DELETE CASCADE,
    user_id    INT         NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_review_likes PRIMARY KEY (review_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_review_likes_user_id ON review_likes (user_id);

CREATE TABLE IF NOT EXISTS review_reply_likes (
    reply_id   INT         NOT NULL REFERENCES review_replies(reply_id) ON DELETE CASCADE,
    user_id    INT         NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_review_reply_likes PRIMARY KEY (reply_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_review_reply_likes_user_id ON review_reply_likes (user_id);

-- Predefined chips users can attach to a review.
-- Each tag belongs to a `dimension` (e.g. fuel_consumption); positive/negative tags
-- in the same dimension form a pair. Asymmetric dimensions (positive-only or
-- negative-only) are intentional — the future recommender simply has evidence in
-- one direction for those.
CREATE TABLE IF NOT EXISTS review_tags (
    tag_id     SMALLSERIAL  PRIMARY KEY,
    code       VARCHAR(40)  NOT NULL UNIQUE,
    label_es   VARCHAR(80)  NOT NULL,
    sentiment  VARCHAR(10)  NOT NULL,
    dimension  VARCHAR(40)  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE review_tags DROP CONSTRAINT IF EXISTS chk_review_tags_sentiment;
ALTER TABLE review_tags
    ADD CONSTRAINT chk_review_tags_sentiment
    CHECK (sentiment IN ('positive', 'negative'));

CREATE INDEX IF NOT EXISTS idx_review_tags_dimension ON review_tags (dimension);

CREATE TABLE IF NOT EXISTS review_tag_assignments (
    review_id  INT         NOT NULL REFERENCES reviews(review_id)    ON DELETE CASCADE,
    tag_id     SMALLINT    NOT NULL REFERENCES review_tags(tag_id)   ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_review_tag_assignments PRIMARY KEY (review_id, tag_id)
);

CREATE INDEX IF NOT EXISTS idx_review_tag_assignments_tag_id ON review_tag_assignments (tag_id);

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
-- Car & CarRequest spec columns
-- ============================================================

ALTER TABLE cars ADD COLUMN IF NOT EXISTS fuel_type        VARCHAR(20);
ALTER TABLE cars ADD COLUMN IF NOT EXISTS horsepower       INT;
ALTER TABLE cars ADD COLUMN IF NOT EXISTS airbag_count     INT;
ALTER TABLE cars ADD COLUMN IF NOT EXISTS transmission     VARCHAR(20);
ALTER TABLE cars ADD COLUMN IF NOT EXISTS fuel_consumption NUMERIC(4,1);
ALTER TABLE cars ADD COLUMN IF NOT EXISTS max_speed_kmh    INT;
ALTER TABLE cars ADD COLUMN IF NOT EXISTS price_usd        NUMERIC(12,2);

ALTER TABLE car_requests ADD COLUMN IF NOT EXISTS fuel_type        VARCHAR(20);
ALTER TABLE car_requests ADD COLUMN IF NOT EXISTS horsepower       INT;
ALTER TABLE car_requests ADD COLUMN IF NOT EXISTS airbag_count     INT;
ALTER TABLE car_requests ADD COLUMN IF NOT EXISTS transmission     VARCHAR(20);
ALTER TABLE car_requests ADD COLUMN IF NOT EXISTS fuel_consumption NUMERIC(4,1);
ALTER TABLE car_requests ADD COLUMN IF NOT EXISTS max_speed_kmh    INT;
ALTER TABLE car_requests ADD COLUMN IF NOT EXISTS price_usd        NUMERIC(12,2);

ALTER TABLE cars ADD COLUMN IF NOT EXISTS year INT;
ALTER TABLE car_requests ADD COLUMN IF NOT EXISTS year INT;

ALTER TABLE cars DROP CONSTRAINT IF EXISTS uq_cars_brand_model_body_type;
ALTER TABLE cars DROP CONSTRAINT IF EXISTS uq_cars_brand_model_body_type_year;
ALTER TABLE cars
    ADD CONSTRAINT uq_cars_brand_model_body_type_year
    UNIQUE NULLS NOT DISTINCT (brand_id, model, body_type_id, year);

ALTER TABLE cars DROP CONSTRAINT IF EXISTS chk_cars_price_usd;
ALTER TABLE cars
    ADD CONSTRAINT chk_cars_price_usd
    CHECK (price_usd IS NULL OR price_usd > 0);

ALTER TABLE car_requests DROP CONSTRAINT IF EXISTS chk_car_requests_price_usd;
ALTER TABLE car_requests
    ADD CONSTRAINT chk_car_requests_price_usd
    CHECK (price_usd IS NULL OR price_usd > 0);

ALTER TABLE cars DROP CONSTRAINT IF EXISTS chk_cars_year;
ALTER TABLE cars
    ADD CONSTRAINT chk_cars_year
    CHECK (year IS NULL OR year BETWEEN 1886 AND 2100);

ALTER TABLE car_requests DROP CONSTRAINT IF EXISTS chk_car_requests_year;
ALTER TABLE car_requests
    ADD CONSTRAINT chk_car_requests_year
    CHECK (year IS NULL OR year BETWEEN 1886 AND 2100);

ALTER TABLE cars DROP CONSTRAINT IF EXISTS chk_cars_fuel_type;
ALTER TABLE cars
    ADD CONSTRAINT chk_cars_fuel_type
    CHECK (fuel_type IS NULL OR fuel_type IN ('combustion', 'hybrid', 'electric'));

ALTER TABLE cars DROP CONSTRAINT IF EXISTS chk_cars_transmission;
ALTER TABLE cars
    ADD CONSTRAINT chk_cars_transmission
    CHECK (transmission IS NULL OR transmission IN ('manual', 'automatic'));

ALTER TABLE cars DROP CONSTRAINT IF EXISTS chk_cars_specs_ranges;
ALTER TABLE cars
    ADD CONSTRAINT chk_cars_specs_ranges
    CHECK (
        (horsepower IS NULL OR horsepower BETWEEN 1 AND 2000)
        AND (airbag_count IS NULL OR airbag_count BETWEEN 0 AND 30)
        AND (fuel_consumption IS NULL OR fuel_consumption BETWEEN 0.0 AND 99.9)
        AND (max_speed_kmh IS NULL OR max_speed_kmh BETWEEN 1 AND 600)
    );

ALTER TABLE car_requests DROP CONSTRAINT IF EXISTS chk_car_requests_fuel_type;
ALTER TABLE car_requests
    ADD CONSTRAINT chk_car_requests_fuel_type
    CHECK (fuel_type IS NULL OR fuel_type IN ('combustion', 'hybrid', 'electric'));

ALTER TABLE car_requests DROP CONSTRAINT IF EXISTS chk_car_requests_transmission;
ALTER TABLE car_requests
    ADD CONSTRAINT chk_car_requests_transmission
    CHECK (transmission IS NULL OR transmission IN ('manual', 'automatic'));

ALTER TABLE car_requests DROP CONSTRAINT IF EXISTS chk_car_requests_specs_ranges;
ALTER TABLE car_requests
    ADD CONSTRAINT chk_car_requests_specs_ranges
    CHECK (
        (horsepower IS NULL OR horsepower BETWEEN 1 AND 2000)
        AND (airbag_count IS NULL OR airbag_count BETWEEN 0 AND 30)
        AND (fuel_consumption IS NULL OR fuel_consumption BETWEEN 0.0 AND 99.9)
        AND (max_speed_kmh IS NULL OR max_speed_kmh BETWEEN 1 AND 600)
    );

-- ============================================================
-- Catalog suggestion requests (brand / body type)
-- ============================================================

CREATE TABLE IF NOT EXISTS brand_requests (
    brand_request_id     BIGSERIAL    PRIMARY KEY,
    submitted_by_user_id INT          REFERENCES users(user_id) ON DELETE SET NULL,
    submitter_email      VARCHAR(100),
    name                 VARCHAR(80)  NOT NULL,
    comments             TEXT,
    status               VARCHAR(20)  NOT NULL DEFAULT 'pending',
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_brand_requests_submitter_identity
        CHECK (submitted_by_user_id IS NOT NULL OR submitter_email IS NOT NULL),
    CONSTRAINT chk_brand_requests_status
        CHECK (status IN ('pending', 'approved', 'rejected'))
);

ALTER TABLE brand_requests ADD COLUMN IF NOT EXISTS comments TEXT;

CREATE INDEX IF NOT EXISTS idx_brand_requests_status ON brand_requests (status);
CREATE INDEX IF NOT EXISTS idx_brand_requests_submitted_by_user_id ON brand_requests (submitted_by_user_id);

CREATE TABLE IF NOT EXISTS body_type_requests (
    body_type_request_id BIGSERIAL    PRIMARY KEY,
    submitted_by_user_id INT          REFERENCES users(user_id) ON DELETE SET NULL,
    submitter_email      VARCHAR(100),
    name                 VARCHAR(80)  NOT NULL,
    comments             TEXT,
    status               VARCHAR(20)  NOT NULL DEFAULT 'pending',
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_body_type_requests_submitter_identity
        CHECK (submitted_by_user_id IS NOT NULL OR submitter_email IS NOT NULL),
    CONSTRAINT chk_body_type_requests_status
        CHECK (status IN ('pending', 'approved', 'rejected'))
);

ALTER TABLE body_type_requests ADD COLUMN IF NOT EXISTS comments TEXT;

CREATE INDEX IF NOT EXISTS idx_body_type_requests_status ON body_type_requests (status);
CREATE INDEX IF NOT EXISTS idx_body_type_requests_submitted_by_user_id ON body_type_requests (submitted_by_user_id);

CREATE TABLE IF NOT EXISTS admin_requests (
    admin_request_id     BIGSERIAL    PRIMARY KEY,
    submitted_by_user_id INT          NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    submitter_email      VARCHAR(100),
    motivation           TEXT         NOT NULL,
    bio                  TEXT         NOT NULL,
    justification        TEXT         NOT NULL,
    status               VARCHAR(20)  NOT NULL DEFAULT 'pending',
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_admin_requests_status
        CHECK (status IN ('pending', 'approved', 'rejected'))
);

CREATE INDEX IF NOT EXISTS idx_admin_requests_status ON admin_requests (status);
CREATE INDEX IF NOT EXISTS idx_admin_requests_submitted_by_user_id ON admin_requests (submitted_by_user_id);

-- ============================================================
-- Seed data
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

-- Populate spec fields for seed cars (idempotent UPDATEs)
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

UPDATE cars SET fuel_type='hybrid',     horsepower=600, airbag_count=10, transmission='automatic', fuel_consumption=11.5, max_speed_kmh=280
WHERE car_id=(SELECT c.car_id FROM cars c JOIN brands b ON c.brand_id=b.brand_id WHERE b.name='Audi'      AND c.model='RS6 Avant');

-- ============================================================
-- Review tag seeds
-- ============================================================

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
