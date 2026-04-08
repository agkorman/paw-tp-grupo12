CREATE TABLE IF NOT EXISTS users (
    user_id    SERIAL PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL DEFAULT 'user',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS brands (
    brand_id     BIGSERIAL PRIMARY KEY,
    name         VARCHAR(80) NOT NULL UNIQUE,
    image_url    VARCHAR(500),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS body_types (
    body_type_id SMALLSERIAL PRIMARY KEY,
    name         VARCHAR(50) NOT NULL UNIQUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cars (
    car_id         BIGSERIAL PRIMARY KEY,
    brand_id       BIGINT NOT NULL
                  REFERENCES brands(brand_id) ON DELETE RESTRICT,
    model          VARCHAR(120) NOT NULL,
    body_type_id   SMALLINT NOT NULL
                  REFERENCES body_types(body_type_id) ON DELETE RESTRICT,
    description    TEXT,
    image_url      VARCHAR(500),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_cars_brand_model_body_type
        UNIQUE (brand_id, model, body_type_id)
);

CREATE INDEX IF NOT EXISTS idx_cars_brand_id
    ON cars (brand_id);

CREATE INDEX IF NOT EXISTS idx_cars_body_type_id
    ON cars (body_type_id);

CREATE INDEX IF NOT EXISTS idx_cars_brand_body_type
    ON cars (brand_id, body_type_id);

-- Full-text search infrastructure
CREATE EXTENSION IF NOT EXISTS pg_trgm;

ALTER TABLE cars ADD COLUMN IF NOT EXISTS search_vector tsvector;

CREATE INDEX IF NOT EXISTS idx_cars_fts          ON cars   USING GIN (search_vector);
CREATE INDEX IF NOT EXISTS idx_brands_name_trgm  ON brands USING GIN (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_cars_model_trgm   ON cars   USING GIN (model gin_trgm_ops);

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

CREATE TABLE IF NOT EXISTS car_images (
    car_id       BIGINT PRIMARY KEY
                 REFERENCES cars(car_id) ON DELETE CASCADE,
    content_type VARCHAR(100) NOT NULL,
    image_data   BYTEA NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Brands
INSERT INTO brands (name, image_url) SELECT 'Toyota',    NULL WHERE NOT EXISTS (SELECT 1 FROM brands WHERE name = 'Toyota');
INSERT INTO brands (name, image_url) SELECT 'Ford',      NULL WHERE NOT EXISTS (SELECT 1 FROM brands WHERE name = 'Ford');
INSERT INTO brands (name, image_url) SELECT 'Mazda',     NULL WHERE NOT EXISTS (SELECT 1 FROM brands WHERE name = 'Mazda');
INSERT INTO brands (name, image_url) SELECT 'BMW',       NULL WHERE NOT EXISTS (SELECT 1 FROM brands WHERE name = 'BMW');
INSERT INTO brands (name, image_url) SELECT 'Porsche',   NULL WHERE NOT EXISTS (SELECT 1 FROM brands WHERE name = 'Porsche');
INSERT INTO brands (name, image_url) SELECT 'Honda',     NULL WHERE NOT EXISTS (SELECT 1 FROM brands WHERE name = 'Honda');
INSERT INTO brands (name, image_url) SELECT 'Subaru',    NULL WHERE NOT EXISTS (SELECT 1 FROM brands WHERE name = 'Subaru');
INSERT INTO brands (name, image_url) SELECT 'Nissan',    NULL WHERE NOT EXISTS (SELECT 1 FROM brands WHERE name = 'Nissan');
INSERT INTO brands (name, image_url) SELECT 'Chevrolet', NULL WHERE NOT EXISTS (SELECT 1 FROM brands WHERE name = 'Chevrolet');
INSERT INTO brands (name, image_url) SELECT 'Audi',      NULL WHERE NOT EXISTS (SELECT 1 FROM brands WHERE name = 'Audi');

-- Body types
INSERT INTO body_types (name) SELECT 'Coupe'     WHERE NOT EXISTS (SELECT 1 FROM body_types WHERE name = 'Coupe');
INSERT INTO body_types (name) SELECT 'Sedan'     WHERE NOT EXISTS (SELECT 1 FROM body_types WHERE name = 'Sedan');
INSERT INTO body_types (name) SELECT 'Roadster'  WHERE NOT EXISTS (SELECT 1 FROM body_types WHERE name = 'Roadster');
INSERT INTO body_types (name) SELECT 'Hatchback' WHERE NOT EXISTS (SELECT 1 FROM body_types WHERE name = 'Hatchback');
INSERT INTO body_types (name) SELECT 'Estate'    WHERE NOT EXISTS (SELECT 1 FROM body_types WHERE name = 'Estate');

-- Cars
INSERT INTO cars (brand_id, model, body_type_id, description, image_url)
SELECT b.brand_id, 'GR Supra', bt.body_type_id,
       'The fifth-generation Supra, co-developed with BMW under the GR sub-brand, revived the iconic nameplate in 2019 after a 17-year hiatus. Powered by a turbocharged inline-six shared with the BMW Z4 G29.',
       'https://upload.wikimedia.org/wikipedia/commons/a/ac/2020_Toyota_GR_Supra_3.0.jpg'
FROM brands b
JOIN body_types bt ON bt.name = 'Coupe'
WHERE b.name = 'Toyota'
  AND NOT EXISTS (
      SELECT 1 FROM cars c
      WHERE c.brand_id = b.brand_id AND c.model = 'GR Supra' AND c.body_type_id = bt.body_type_id
  );

INSERT INTO cars (brand_id, model, body_type_id, description, image_url)
SELECT b.brand_id, 'Mustang', bt.body_type_id,
       'The sixth-generation Mustang was the first to offer independent rear suspension globally and introduced the turbocharged EcoBoost four-cylinder option alongside its iconic V8.',
       'https://upload.wikimedia.org/wikipedia/commons/7/73/2018_Ford_Mustang_5.0_coupe.jpg'
FROM brands b
JOIN body_types bt ON bt.name = 'Coupe'
WHERE b.name = 'Ford'
  AND NOT EXISTS (
      SELECT 1 FROM cars c
      WHERE c.brand_id = b.brand_id AND c.model = 'Mustang' AND c.body_type_id = bt.body_type_id
  );

INSERT INTO cars (brand_id, model, body_type_id, description, image_url)
SELECT b.brand_id, 'MX-5 Miata', bt.body_type_id,
       'The fourth-generation MX-5 shed over 100 kg versus its predecessor, returning to the original gram strategy of lightweight engineering. It remains the world''s best-selling two-seat roadster.',
       'https://upload.wikimedia.org/wikipedia/commons/a/a5/20_Mazda_MX-5_Miata_Club.jpg'
FROM brands b
JOIN body_types bt ON bt.name = 'Roadster'
WHERE b.name = 'Mazda'
  AND NOT EXISTS (
      SELECT 1 FROM cars c
      WHERE c.brand_id = b.brand_id AND c.model = 'MX-5 Miata' AND c.body_type_id = bt.body_type_id
  );

INSERT INTO cars (brand_id, model, body_type_id, description, image_url)
SELECT b.brand_id, 'M3', bt.body_type_id,
       'The G80 M3 is powered by the S58 twin-turbocharged inline-six producing up to 503 hp in Competition trim. It was the first M3 offered with optional all-wheel drive and the first to spawn an M3 Touring variant.',
       'https://upload.wikimedia.org/wikipedia/commons/3/36/BMW_M3_%28G80%2C_2022%29_%2852227837026%29_%28cropped%29.jpg'
FROM brands b
JOIN body_types bt ON bt.name = 'Sedan'
WHERE b.name = 'BMW'
  AND NOT EXISTS (
      SELECT 1 FROM cars c
      WHERE c.brand_id = b.brand_id AND c.model = 'M3' AND c.body_type_id = bt.body_type_id
  );

INSERT INTO cars (brand_id, model, body_type_id, description, image_url)
SELECT b.brand_id, '911', bt.body_type_id,
       'The 992-generation 911 is 45 mm wider than its predecessor and adopts an all-aluminium body structure. It retains the iconic rear-mounted flat-six engine while featuring electric pop-out door handles.',
       'https://upload.wikimedia.org/wikipedia/commons/b/b4/Porsche_911_992_2.jpg'
FROM brands b
JOIN body_types bt ON bt.name = 'Coupe'
WHERE b.name = 'Porsche'
  AND NOT EXISTS (
      SELECT 1 FROM cars c
      WHERE c.brand_id = b.brand_id AND c.model = '911' AND c.body_type_id = bt.body_type_id
  );

INSERT INTO cars (brand_id, model, body_type_id, description, image_url)
SELECT b.brand_id, 'Civic Type R', bt.body_type_id,
       'The FL5 Type R carries a 330 PS K20C1 turbocharged engine and introduces reshaped wider rear body panels. It features Honda''s LogR data logger for track use and a more refined design than its FK8 predecessor.',
       'https://upload.wikimedia.org/wikipedia/commons/4/4c/2022_Honda_Civic_Type_R_1.jpg'
FROM brands b
JOIN body_types bt ON bt.name = 'Hatchback'
WHERE b.name = 'Honda'
  AND NOT EXISTS (
      SELECT 1 FROM cars c
      WHERE c.brand_id = b.brand_id AND c.model = 'Civic Type R' AND c.body_type_id = bt.body_type_id
  );

INSERT INTO cars (brand_id, model, body_type_id, description, image_url)
SELECT b.brand_id, 'WRX STI', bt.body_type_id,
       'The VA-generation WRX STI was the first offered only as a sedan, powered by the EJ257 turbocharged flat-four producing 305 hp paired with a 6-speed manual and symmetrical all-wheel drive.',
       'https://upload.wikimedia.org/wikipedia/commons/c/c0/%2715_Subaru_WRX_STI_%28MIAS_%2715%29.jpg'
FROM brands b
JOIN body_types bt ON bt.name = 'Sedan'
WHERE b.name = 'Subaru'
  AND NOT EXISTS (
      SELECT 1 FROM cars c
      WHERE c.brand_id = b.brand_id AND c.model = 'WRX STI' AND c.body_type_id = bt.body_type_id
  );

INSERT INTO cars (brand_id, model, body_type_id, description, image_url)
SELECT b.brand_id, 'GT-R', bt.body_type_id,
       'The R35 GT-R was built around a hand-assembled VR38DETT twin-turbocharged V6, with each engine signed by one of nine certified Takumi craftsmen. Its combination of all-wheel drive and launch control made it a benchmark supercar-slayer.',
       'https://upload.wikimedia.org/wikipedia/commons/9/91/2009_Nissan_GT-R_Black_Edition_1.jpg'
FROM brands b
JOIN body_types bt ON bt.name = 'Coupe'
WHERE b.name = 'Nissan'
  AND NOT EXISTS (
      SELECT 1 FROM cars c
      WHERE c.brand_id = b.brand_id AND c.model = 'GT-R' AND c.body_type_id = bt.body_type_id
  );

INSERT INTO cars (brand_id, model, body_type_id, description, image_url)
SELECT b.brand_id, 'Camaro', bt.body_type_id,
       'Built on GM''s Alpha platform shared with the Cadillac ATS, the sixth-gen Camaro shed over 200 lbs versus its predecessor. Available engines ranged from a turbocharged 2.0L four-cylinder to a supercharged 6.2L V8 in the ZL1.',
       'https://upload.wikimedia.org/wikipedia/commons/4/41/2016-18_Chevrolet_Camaro_VI_rear%2C_7.20.19.jpg'
FROM brands b
JOIN body_types bt ON bt.name = 'Coupe'
WHERE b.name = 'Chevrolet'
  AND NOT EXISTS (
      SELECT 1 FROM cars c
      WHERE c.brand_id = b.brand_id AND c.model = 'Camaro' AND c.body_type_id = bt.body_type_id
  );

INSERT INTO cars (brand_id, model, body_type_id, description, image_url)
SELECT b.brand_id, 'RS6 Avant', bt.body_type_id,
       'The C8 RS6 Avant is powered by a 4.0L twin-turbo V8 with a 48V mild-hybrid system producing 600 PS. It was the first RS6 Avant sold in North America since the C5 generation and the first RS6 to use a hybrid drivetrain.',
       'https://upload.wikimedia.org/wikipedia/commons/0/00/Audi_RS6_Avant_C8_1X7A0305.jpg'
FROM brands b
JOIN body_types bt ON bt.name = 'Estate'
WHERE b.name = 'Audi'
  AND NOT EXISTS (
      SELECT 1 FROM cars c
      WHERE c.brand_id = b.brand_id AND c.model = 'RS6 Avant' AND c.body_type_id = bt.body_type_id
  );

-- Backfill search_vector for any rows that pre-date the trigger
UPDATE cars c
SET search_vector = cars_build_search_vector(
    (SELECT name FROM brands     WHERE brand_id     = c.brand_id),
    c.model,
    (SELECT name FROM body_types WHERE body_type_id = c.body_type_id),
    c.description
)
WHERE c.search_vector IS NULL;

CREATE TABLE IF NOT EXISTS reviews (
    review_id        SERIAL PRIMARY KEY,
    user_id          INT          REFERENCES users(user_id),
    reviewer_email   VARCHAR(100),
    car_id           BIGINT       NOT NULL REFERENCES cars(car_id),
    rating           NUMERIC(3,1) NOT NULL CHECK (rating >= 0.0 AND rating <= 5.0),
    title            VARCHAR(200) NOT NULL,
    body             TEXT         NOT NULL,
    ownership_status VARCHAR(20),
    model_year       INT,
    mileage_km       INT,
    would_recommend  BOOLEAN,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_reviews_reviewer_identity
        CHECK (user_id IS NOT NULL OR reviewer_email IS NOT NULL)
);

CREATE INDEX IF NOT EXISTS idx_reviews_car_id
    ON reviews (car_id);
