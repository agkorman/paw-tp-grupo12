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

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE IF NOT EXISTS cars (
    car_id         BIGSERIAL    PRIMARY KEY,
    brand_id       BIGINT       NOT NULL REFERENCES brands(brand_id)         ON DELETE RESTRICT,
    model          VARCHAR(120) NOT NULL,
    body_type_id   SMALLINT     NOT NULL REFERENCES body_types(body_type_id) ON DELETE RESTRICT,
    description    TEXT,
    search_vector  tsvector,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_cars_brand_model_body_type UNIQUE (brand_id, model, body_type_id)
);

CREATE INDEX IF NOT EXISTS idx_cars_brand_id        ON cars   (brand_id);
CREATE INDEX IF NOT EXISTS idx_cars_body_type_id    ON cars   (body_type_id);
CREATE INDEX IF NOT EXISTS idx_cars_brand_body_type ON cars   (brand_id, body_type_id);
CREATE INDEX IF NOT EXISTS idx_cars_fts             ON cars   USING GIN (search_vector);
CREATE INDEX IF NOT EXISTS idx_cars_model_trgm      ON cars   USING GIN (model gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_brands_name_trgm     ON brands USING GIN (name gin_trgm_ops);

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

CREATE TABLE IF NOT EXISTS car_images (
    car_id       BIGINT       PRIMARY KEY REFERENCES cars(car_id) ON DELETE CASCADE,
    content_type VARCHAR(100) NOT NULL,
    image_data   BYTEA        NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
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

CREATE INDEX IF NOT EXISTS idx_reviews_car_id ON reviews (car_id);

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

INSERT INTO cars (brand_id, model, body_type_id, description)
SELECT b.brand_id, 'GR Supra', bt.body_type_id,
       'The fifth-generation Supra, co-developed with BMW under the GR sub-brand, revived the iconic nameplate in 2019 after a 17-year hiatus. Powered by a turbocharged inline-six shared with the BMW Z4 G29.'
FROM brands b, body_types bt WHERE b.name = 'Toyota' AND bt.name = 'Coupe'
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type DO NOTHING;

INSERT INTO cars (brand_id, model, body_type_id, description)
SELECT b.brand_id, 'Mustang', bt.body_type_id,
       'The sixth-generation Mustang was the first to offer independent rear suspension globally and introduced the turbocharged EcoBoost four-cylinder option alongside its iconic V8.'
FROM brands b, body_types bt WHERE b.name = 'Ford' AND bt.name = 'Coupe'
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type DO NOTHING;

INSERT INTO cars (brand_id, model, body_type_id, description)
SELECT b.brand_id, 'MX-5 Miata', bt.body_type_id,
       'The fourth-generation MX-5 shed over 100 kg versus its predecessor, returning to the original gram strategy of lightweight engineering. It remains the world''s best-selling two-seat roadster.'
FROM brands b, body_types bt WHERE b.name = 'Mazda' AND bt.name = 'Roadster'
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type DO NOTHING;

INSERT INTO cars (brand_id, model, body_type_id, description)
SELECT b.brand_id, 'M3', bt.body_type_id,
       'The G80 M3 is powered by the S58 twin-turbocharged inline-six producing up to 503 hp in Competition trim. It was the first M3 offered with optional all-wheel drive and the first to spawn an M3 Touring variant.'
FROM brands b, body_types bt WHERE b.name = 'BMW' AND bt.name = 'Sedan'
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type DO NOTHING;

INSERT INTO cars (brand_id, model, body_type_id, description)
SELECT b.brand_id, '911', bt.body_type_id,
       'The 992-generation 911 is 45 mm wider than its predecessor and adopts an all-aluminium body structure. It retains the iconic rear-mounted flat-six engine while featuring electric pop-out door handles.'
FROM brands b, body_types bt WHERE b.name = 'Porsche' AND bt.name = 'Coupe'
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type DO NOTHING;

INSERT INTO cars (brand_id, model, body_type_id, description)
SELECT b.brand_id, 'Civic Type R', bt.body_type_id,
       'The FL5 Type R carries a 330 PS K20C1 turbocharged engine and introduces reshaped wider rear body panels. It features Honda''s LogR data logger for track use and a more refined design than its FK8 predecessor.'
FROM brands b, body_types bt WHERE b.name = 'Honda' AND bt.name = 'Hatchback'
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type DO NOTHING;

INSERT INTO cars (brand_id, model, body_type_id, description)
SELECT b.brand_id, 'WRX STI', bt.body_type_id,
       'The VA-generation WRX STI was the first offered only as a sedan, powered by the EJ257 turbocharged flat-four producing 305 hp paired with a 6-speed manual and symmetrical all-wheel drive.'
FROM brands b, body_types bt WHERE b.name = 'Subaru' AND bt.name = 'Sedan'
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type DO NOTHING;

INSERT INTO cars (brand_id, model, body_type_id, description)
SELECT b.brand_id, 'GT-R', bt.body_type_id,
       'The R35 GT-R was built around a hand-assembled VR38DETT twin-turbocharged V6, with each engine signed by one of nine certified Takumi craftsmen. Its combination of all-wheel drive and launch control made it a benchmark supercar-slayer.'
FROM brands b, body_types bt WHERE b.name = 'Nissan' AND bt.name = 'Coupe'
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type DO NOTHING;

INSERT INTO cars (brand_id, model, body_type_id, description)
SELECT b.brand_id, 'Camaro', bt.body_type_id,
       'Built on GM''s Alpha platform shared with the Cadillac ATS, the sixth-gen Camaro shed over 200 lbs versus its predecessor. Available engines ranged from a turbocharged 2.0L four-cylinder to a supercharged 6.2L V8 in the ZL1.'
FROM brands b, body_types bt WHERE b.name = 'Chevrolet' AND bt.name = 'Coupe'
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type DO NOTHING;

INSERT INTO cars (brand_id, model, body_type_id, description)
SELECT b.brand_id, 'RS6 Avant', bt.body_type_id,
       'The C8 RS6 Avant is powered by a 4.0L twin-turbo V8 with a 48V mild-hybrid system producing 600 PS. It was the first RS6 Avant sold in North America since the C5 generation and the first RS6 to use a hybrid drivetrain.'
FROM brands b, body_types bt WHERE b.name = 'Audi' AND bt.name = 'Estate'
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type DO NOTHING;
