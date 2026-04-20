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
    description    TEXT,
    search_vector  tsvector,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_cars_brand_model_body_type UNIQUE (brand_id, model, body_type_id)
);

CREATE INDEX IF NOT EXISTS idx_cars_brand_id        ON cars   (brand_id);
CREATE INDEX IF NOT EXISTS idx_cars_body_type_id    ON cars   (body_type_id);
CREATE INDEX IF NOT EXISTS idx_cars_brand_body_type ON cars   (brand_id, body_type_id);
CREATE INDEX IF NOT EXISTS idx_cars_fts             ON cars   USING GIN (search_vector);

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
