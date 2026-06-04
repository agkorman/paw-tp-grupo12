-- ============================================================
-- Tables
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    user_id    SERIAL       PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL DEFAULT 'user',
    preferred_locale VARCHAR(10) NOT NULL DEFAULT 'es',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE users
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS preferred_locale VARCHAR(10) NOT NULL DEFAULT 'es';

ALTER TABLE users
    DROP CONSTRAINT IF EXISTS chk_users_preferred_locale;

ALTER TABLE users
    ADD CONSTRAINT chk_users_preferred_locale
        CHECK (preferred_locale IN ('es', 'en'));

CREATE UNIQUE INDEX IF NOT EXISTS users_username_normalized_unique_idx
    ON users (LOWER(BTRIM(username)));

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

ALTER TABLE car_images
    ALTER COLUMN image_id SET NOT NULL;

ALTER TABLE car_images
    ALTER COLUMN display_order SET DEFAULT 0;

ALTER TABLE car_images
    ALTER COLUMN display_order SET NOT NULL;

ALTER SEQUENCE car_images_image_id_seq OWNED BY car_images.image_id;

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

ALTER TABLE reviews
    DROP CONSTRAINT IF EXISTS reviews_car_id_fkey;

ALTER TABLE reviews
    DROP CONSTRAINT IF EXISTS fk_reviews_car_id;

ALTER TABLE reviews
    ADD CONSTRAINT fk_reviews_car_id
    FOREIGN KEY (car_id) REFERENCES cars(car_id) ON DELETE CASCADE;

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
    CHECK (year IS NULL OR year BETWEEN 1950 AND 2026) NOT VALID;

ALTER TABLE car_requests DROP CONSTRAINT IF EXISTS chk_car_requests_year;
ALTER TABLE car_requests
    ADD CONSTRAINT chk_car_requests_year
    CHECK (year IS NULL OR year BETWEEN 1950 AND 2026) NOT VALID;

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
-- Legacy/manual seed data extracted to schema.deprecated.sql
-- ============================================================

-- ============================================================
-- Column type migrations: widen INT/SMALLINT → BIGINT so that
-- Hibernate schema validation passes (Java `long` maps to int8).
-- All steps are idempotent: DROP IF EXISTS + ADD, and widening
-- an already-BIGINT column is a no-op in PostgreSQL. Keep this
-- block transactional so FK drops are rolled back if any later
-- statement fails.
-- ============================================================

BEGIN;

-- ---- users.user_id (SERIAL=int4 → BIGINT) -------------------
ALTER TABLE user_follows         DROP CONSTRAINT IF EXISTS user_follows_follower_id_fkey;
ALTER TABLE user_follows         DROP CONSTRAINT IF EXISTS user_follows_followed_id_fkey;
ALTER TABLE car_favorites        DROP CONSTRAINT IF EXISTS car_favorites_user_id_fkey;
ALTER TABLE car_requests         DROP CONSTRAINT IF EXISTS car_requests_submitted_by_user_id_fkey;
ALTER TABLE admin_requests       DROP CONSTRAINT IF EXISTS admin_requests_submitted_by_user_id_fkey;
ALTER TABLE reviews              DROP CONSTRAINT IF EXISTS reviews_user_id_fkey;
ALTER TABLE review_replies       DROP CONSTRAINT IF EXISTS review_replies_user_id_fkey;
ALTER TABLE review_likes         DROP CONSTRAINT IF EXISTS review_likes_user_id_fkey;
ALTER TABLE review_reply_likes   DROP CONSTRAINT IF EXISTS review_reply_likes_user_id_fkey;
ALTER TABLE brand_requests       DROP CONSTRAINT IF EXISTS brand_requests_submitted_by_user_id_fkey;
ALTER TABLE body_type_requests   DROP CONSTRAINT IF EXISTS body_type_requests_submitted_by_user_id_fkey;

ALTER TABLE users                ALTER COLUMN user_id              TYPE BIGINT;
ALTER TABLE user_follows         ALTER COLUMN follower_id          TYPE BIGINT;
ALTER TABLE user_follows         ALTER COLUMN followed_id          TYPE BIGINT;
ALTER TABLE car_favorites        ALTER COLUMN user_id              TYPE BIGINT;
ALTER TABLE car_requests         ALTER COLUMN submitted_by_user_id TYPE BIGINT;
ALTER TABLE admin_requests       ALTER COLUMN submitted_by_user_id TYPE BIGINT;
ALTER TABLE reviews              ALTER COLUMN user_id              TYPE BIGINT;
ALTER TABLE review_replies       ALTER COLUMN user_id              TYPE BIGINT;
ALTER TABLE review_likes         ALTER COLUMN user_id              TYPE BIGINT;
ALTER TABLE review_reply_likes   ALTER COLUMN user_id              TYPE BIGINT;
ALTER TABLE brand_requests       ALTER COLUMN submitted_by_user_id TYPE BIGINT;
ALTER TABLE body_type_requests   ALTER COLUMN submitted_by_user_id TYPE BIGINT;

ALTER TABLE user_follows       ADD CONSTRAINT user_follows_follower_id_fkey                    FOREIGN KEY (follower_id)          REFERENCES users(user_id) ON DELETE CASCADE;
ALTER TABLE user_follows       ADD CONSTRAINT user_follows_followed_id_fkey                    FOREIGN KEY (followed_id)          REFERENCES users(user_id) ON DELETE CASCADE;
ALTER TABLE car_favorites      ADD CONSTRAINT car_favorites_user_id_fkey                       FOREIGN KEY (user_id)              REFERENCES users(user_id) ON DELETE CASCADE;
ALTER TABLE car_requests       ADD CONSTRAINT car_requests_submitted_by_user_id_fkey           FOREIGN KEY (submitted_by_user_id) REFERENCES users(user_id) ON DELETE SET NULL;
ALTER TABLE admin_requests     ADD CONSTRAINT admin_requests_submitted_by_user_id_fkey         FOREIGN KEY (submitted_by_user_id) REFERENCES users(user_id) ON DELETE CASCADE;
ALTER TABLE reviews            ADD CONSTRAINT reviews_user_id_fkey                             FOREIGN KEY (user_id)              REFERENCES users(user_id);
ALTER TABLE review_replies     ADD CONSTRAINT review_replies_user_id_fkey                      FOREIGN KEY (user_id)              REFERENCES users(user_id);
ALTER TABLE review_likes       ADD CONSTRAINT review_likes_user_id_fkey                        FOREIGN KEY (user_id)              REFERENCES users(user_id) ON DELETE CASCADE;
ALTER TABLE review_reply_likes ADD CONSTRAINT review_reply_likes_user_id_fkey                  FOREIGN KEY (user_id)              REFERENCES users(user_id) ON DELETE CASCADE;
ALTER TABLE brand_requests     ADD CONSTRAINT brand_requests_submitted_by_user_id_fkey         FOREIGN KEY (submitted_by_user_id) REFERENCES users(user_id) ON DELETE SET NULL;
ALTER TABLE body_type_requests ADD CONSTRAINT body_type_requests_submitted_by_user_id_fkey     FOREIGN KEY (submitted_by_user_id) REFERENCES users(user_id) ON DELETE SET NULL;

-- ---- body_types.body_type_id (SMALLSERIAL=int2 → BIGINT) -----
ALTER TABLE cars         DROP CONSTRAINT IF EXISTS cars_body_type_id_fkey;
ALTER TABLE car_requests DROP CONSTRAINT IF EXISTS car_requests_body_type_id_fkey;

ALTER TABLE body_types   ALTER COLUMN body_type_id TYPE BIGINT;
ALTER TABLE cars         ALTER COLUMN body_type_id TYPE BIGINT;
ALTER TABLE car_requests ALTER COLUMN body_type_id TYPE BIGINT;

ALTER TABLE cars         ADD CONSTRAINT cars_body_type_id_fkey         FOREIGN KEY (body_type_id) REFERENCES body_types(body_type_id) ON DELETE RESTRICT;
ALTER TABLE car_requests ADD CONSTRAINT car_requests_body_type_id_fkey FOREIGN KEY (body_type_id) REFERENCES body_types(body_type_id) ON DELETE RESTRICT;

-- ---- reviews.review_id (SERIAL=int4 → BIGINT) ----------------
ALTER TABLE review_replies         DROP CONSTRAINT IF EXISTS review_replies_review_id_fkey;
ALTER TABLE review_likes           DROP CONSTRAINT IF EXISTS review_likes_review_id_fkey;
ALTER TABLE review_tag_assignments DROP CONSTRAINT IF EXISTS review_tag_assignments_review_id_fkey;

ALTER TABLE reviews                ALTER COLUMN review_id TYPE BIGINT;
ALTER TABLE review_replies         ALTER COLUMN review_id TYPE BIGINT;
ALTER TABLE review_likes           ALTER COLUMN review_id TYPE BIGINT;
ALTER TABLE review_tag_assignments ALTER COLUMN review_id TYPE BIGINT;

ALTER TABLE review_replies         ADD CONSTRAINT review_replies_review_id_fkey         FOREIGN KEY (review_id) REFERENCES reviews(review_id) ON DELETE CASCADE;
ALTER TABLE review_likes           ADD CONSTRAINT review_likes_review_id_fkey           FOREIGN KEY (review_id) REFERENCES reviews(review_id) ON DELETE CASCADE;
ALTER TABLE review_tag_assignments ADD CONSTRAINT review_tag_assignments_review_id_fkey FOREIGN KEY (review_id) REFERENCES reviews(review_id) ON DELETE CASCADE;

-- ---- review_replies.reply_id (SERIAL=int4 → BIGINT) ----------
ALTER TABLE review_reply_likes DROP CONSTRAINT IF EXISTS review_reply_likes_reply_id_fkey;

ALTER TABLE review_replies     ALTER COLUMN reply_id TYPE BIGINT;
ALTER TABLE review_reply_likes ALTER COLUMN reply_id TYPE BIGINT;

ALTER TABLE review_reply_likes ADD CONSTRAINT review_reply_likes_reply_id_fkey FOREIGN KEY (reply_id) REFERENCES review_replies(reply_id) ON DELETE CASCADE;

-- ---- review_images ------------------------------------------
CREATE SEQUENCE IF NOT EXISTS review_images_image_id_seq;

CREATE TABLE IF NOT EXISTS review_images (
    image_id      BIGINT       PRIMARY KEY DEFAULT nextval('review_images_image_id_seq'),
    review_id     BIGINT       NOT NULL REFERENCES reviews(review_id) ON DELETE CASCADE,
    display_order INT          NOT NULL DEFAULT 0,
    content_type  VARCHAR(100) NOT NULL,
    image_data    BYTEA        NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER SEQUENCE review_images_image_id_seq OWNED BY review_images.image_id;

ALTER TABLE review_images
    DROP CONSTRAINT IF EXISTS chk_review_images_display_order;

ALTER TABLE review_images
    ADD CONSTRAINT chk_review_images_display_order CHECK (display_order >= 0);

CREATE INDEX IF NOT EXISTS idx_review_images_review_id ON review_images (review_id);

-- ============================================================
-- Communities
-- ============================================================

CREATE TABLE IF NOT EXISTS communities (
    community_id        BIGSERIAL    PRIMARY KEY,
    slug                VARCHAR(60)  NOT NULL,
    name                VARCHAR(60)  NOT NULL,
    description         TEXT         NOT NULL,
    search_vector       tsvector,
    created_by_user_id  BIGINT       REFERENCES users(user_id) ON DELETE SET NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE communities ADD COLUMN IF NOT EXISTS search_vector tsvector;

CREATE OR REPLACE FUNCTION communities_build_search_vector(
    p_name        TEXT,
    p_slug        TEXT,
    p_description TEXT
) RETURNS tsvector LANGUAGE sql IMMUTABLE AS $$
    SELECT
        setweight(to_tsvector('simple', COALESCE(p_name,        '')), 'A') ||
        setweight(to_tsvector('simple', COALESCE(p_slug,        '')), 'B') ||
        setweight(to_tsvector('simple', COALESCE(p_description, '')), 'C')
$$;

CREATE OR REPLACE FUNCTION communities_search_vector_trigger_fn()
RETURNS TRIGGER LANGUAGE plpgsql AS
'BEGIN
    NEW.search_vector := communities_build_search_vector(NEW.name, NEW.slug, NEW.description);
    RETURN NEW;
END';

CREATE OR REPLACE TRIGGER communities_search_vector_trigger
    BEFORE INSERT OR UPDATE ON communities
    FOR EACH ROW EXECUTE FUNCTION communities_search_vector_trigger_fn();

CREATE TABLE IF NOT EXISTS community_topics (
    topic_id     SMALLSERIAL  PRIMARY KEY,
    code         VARCHAR(40)  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS community_topic_assignments (
    community_id BIGINT       NOT NULL REFERENCES communities(community_id) ON DELETE CASCADE,
    topic_id     SMALLINT     NOT NULL REFERENCES community_topics(topic_id) ON DELETE CASCADE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_community_topic_assignments PRIMARY KEY (community_id, topic_id)
);

CREATE TABLE IF NOT EXISTS community_memberships (
    community_id BIGINT       NOT NULL REFERENCES communities(community_id) ON DELETE CASCADE,
    user_id      BIGINT       NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    role         VARCHAR(20)  NOT NULL DEFAULT 'member',
    joined_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_community_memberships PRIMARY KEY (community_id, user_id)
);

CREATE TABLE IF NOT EXISTS community_posts (
    post_id          BIGSERIAL    PRIMARY KEY,
    community_id     BIGINT       NOT NULL REFERENCES communities(community_id) ON DELETE CASCADE,
    author_user_id   BIGINT       NOT NULL REFERENCES users(user_id),
    slug             VARCHAR(80)  NOT NULL,
    title            VARCHAR(120) NOT NULL,
    body             TEXT,
    external_url     VARCHAR(2048),
    linked_review_id BIGINT       REFERENCES reviews(review_id) ON DELETE SET NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS community_post_comments (
    comment_id   BIGSERIAL    PRIMARY KEY,
    post_id      BIGINT       NOT NULL REFERENCES community_posts(post_id) ON DELETE CASCADE,
    user_id      BIGINT       NOT NULL REFERENCES users(user_id),
    body         TEXT         NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS community_post_helpful_reactions (
    post_id      BIGINT       NOT NULL REFERENCES community_posts(post_id) ON DELETE CASCADE,
    user_id      BIGINT       NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_community_post_helpful_reactions PRIMARY KEY (post_id, user_id)
);

CREATE TABLE IF NOT EXISTS community_post_comment_helpful_reactions (
    comment_id   BIGINT       NOT NULL REFERENCES community_post_comments(comment_id) ON DELETE CASCADE,
    user_id      BIGINT       NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_community_post_comment_helpful_reactions PRIMARY KEY (comment_id, user_id)
);

CREATE TABLE IF NOT EXISTS community_post_images (
    image_id       BIGSERIAL    PRIMARY KEY,
    post_id        BIGINT       NOT NULL REFERENCES community_posts(post_id) ON DELETE CASCADE,
    display_order  INT          NOT NULL DEFAULT 0,
    content_type   VARCHAR(100) NOT NULL,
    image_data     BYTEA        NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE communities DROP CONSTRAINT IF EXISTS chk_communities_slug_not_blank;
ALTER TABLE communities ADD CONSTRAINT chk_communities_slug_not_blank CHECK (BTRIM(slug) <> '');
ALTER TABLE communities DROP CONSTRAINT IF EXISTS chk_communities_name_not_blank;
ALTER TABLE communities ADD CONSTRAINT chk_communities_name_not_blank CHECK (BTRIM(name) <> '');
ALTER TABLE communities DROP CONSTRAINT IF EXISTS chk_communities_description_not_blank;
ALTER TABLE communities ADD CONSTRAINT chk_communities_description_not_blank CHECK (BTRIM(description) <> '');

ALTER TABLE community_topics DROP CONSTRAINT IF EXISTS chk_community_topics_code_not_blank;
ALTER TABLE community_topics ADD CONSTRAINT chk_community_topics_code_not_blank CHECK (BTRIM(code) <> '');

ALTER TABLE community_topics ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE community_memberships DROP CONSTRAINT IF EXISTS chk_community_memberships_role;
ALTER TABLE community_memberships ADD CONSTRAINT chk_community_memberships_role
    CHECK (role IN ('member', 'moderator'));

ALTER TABLE community_posts DROP CONSTRAINT IF EXISTS chk_community_posts_slug_not_blank;
ALTER TABLE community_posts ADD CONSTRAINT chk_community_posts_slug_not_blank CHECK (BTRIM(slug) <> '');
ALTER TABLE community_posts DROP CONSTRAINT IF EXISTS chk_community_posts_title_not_blank;
ALTER TABLE community_posts ADD CONSTRAINT chk_community_posts_title_not_blank CHECK (BTRIM(title) <> '');
ALTER TABLE community_posts DROP CONSTRAINT IF EXISTS chk_community_posts_body_not_blank;
ALTER TABLE community_posts ADD CONSTRAINT chk_community_posts_body_not_blank CHECK (BTRIM(body) <> '');

ALTER TABLE community_post_comments DROP CONSTRAINT IF EXISTS chk_community_post_comments_body_not_blank;
ALTER TABLE community_post_comments ADD CONSTRAINT chk_community_post_comments_body_not_blank CHECK (BTRIM(body) <> '');

ALTER TABLE community_posts ADD COLUMN IF NOT EXISTS hidden BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE community_post_comments ADD COLUMN IF NOT EXISTS hidden BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE community_post_images DROP CONSTRAINT IF EXISTS chk_community_post_images_display_order;
ALTER TABLE community_post_images ADD CONSTRAINT chk_community_post_images_display_order CHECK (display_order >= 0);

CREATE UNIQUE INDEX IF NOT EXISTS uq_communities_slug ON communities (LOWER(BTRIM(slug)));
CREATE UNIQUE INDEX IF NOT EXISTS uq_community_topics_code ON community_topics (LOWER(BTRIM(code)));
CREATE UNIQUE INDEX IF NOT EXISTS uq_community_posts_slug ON community_posts (community_id, LOWER(BTRIM(slug)));
CREATE UNIQUE INDEX IF NOT EXISTS uq_community_post_images_order ON community_post_images (post_id, display_order);

CREATE INDEX IF NOT EXISTS idx_communities_creator_id ON communities (created_by_user_id);
CREATE INDEX IF NOT EXISTS idx_communities_fts ON communities USING GIN (search_vector);
CREATE INDEX IF NOT EXISTS idx_community_topic_assignments_topic_id ON community_topic_assignments (topic_id);
CREATE INDEX IF NOT EXISTS idx_community_memberships_user_id ON community_memberships (user_id);
CREATE INDEX IF NOT EXISTS idx_community_posts_community_created_at ON community_posts (community_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_community_posts_author_user_id ON community_posts (author_user_id);
CREATE INDEX IF NOT EXISTS idx_community_post_helpful_reactions_user_id ON community_post_helpful_reactions (user_id);
CREATE INDEX IF NOT EXISTS idx_community_post_comment_helpful_reactions_user_id ON community_post_comment_helpful_reactions (user_id);
CREATE INDEX IF NOT EXISTS idx_community_post_comments_post_created_at ON community_post_comments (post_id, created_at ASC);
CREATE INDEX IF NOT EXISTS idx_community_post_comments_user_id ON community_post_comments (user_id);
CREATE INDEX IF NOT EXISTS idx_community_post_images_post_id ON community_post_images (post_id);

COMMIT;
