DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS cars CASCADE;
DROP TABLE IF EXISTS brands CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE IF NOT EXISTS users (
    user_id    SERIAL PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL DEFAULT 'user',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS brands (
    brand_id   SERIAL PRIMARY KEY,
    name       VARCHAR(50)  NOT NULL UNIQUE,
    image_url  VARCHAR(500),
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cars (
    car_id      SERIAL PRIMARY KEY,
    brand_id    INT          NOT NULL REFERENCES brands(brand_id),
    model       VARCHAR(50)  NOT NULL,
    generation  VARCHAR(100),
    body_type   VARCHAR(50),
    description TEXT,
    image_url   VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
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

-- Cars
INSERT INTO cars (brand_id, model, generation, body_type, description, image_url)
SELECT b.brand_id, 'GR Supra', 'A90', 'Coupe',
       'The fifth-generation Supra, co-developed with BMW under the GR sub-brand, revived the iconic nameplate in 2019 after a 17-year hiatus. Powered by a turbocharged inline-six shared with the BMW Z4 G29.',
       'https://upload.wikimedia.org/wikipedia/commons/a/ac/2020_Toyota_GR_Supra_3.0.jpg'
FROM brands b WHERE b.name = 'Toyota' AND NOT EXISTS (SELECT 1 FROM cars c JOIN brands b2 ON c.brand_id = b2.brand_id WHERE b2.name = 'Toyota' AND c.model = 'GR Supra');

INSERT INTO cars (brand_id, model, generation, body_type, description, image_url)
SELECT b.brand_id, 'Mustang', 'S550', 'Coupe',
       'The sixth-generation Mustang was the first to offer independent rear suspension globally and introduced the turbocharged EcoBoost four-cylinder option alongside its iconic V8.',
       'https://upload.wikimedia.org/wikipedia/commons/7/73/2018_Ford_Mustang_5.0_coupe.jpg'
FROM brands b WHERE b.name = 'Ford' AND NOT EXISTS (SELECT 1 FROM cars c JOIN brands b2 ON c.brand_id = b2.brand_id WHERE b2.name = 'Ford' AND c.model = 'Mustang');

INSERT INTO cars (brand_id, model, generation, body_type, description, image_url)
SELECT b.brand_id, 'MX-5 Miata', 'ND', 'Roadster',
       'The fourth-generation MX-5 shed over 100 kg versus its predecessor, returning to the original gram strategy of lightweight engineering. It remains the world''s best-selling two-seat roadster.',
       'https://upload.wikimedia.org/wikipedia/commons/a/a5/20_Mazda_MX-5_Miata_Club.jpg'
FROM brands b WHERE b.name = 'Mazda' AND NOT EXISTS (SELECT 1 FROM cars c JOIN brands b2 ON c.brand_id = b2.brand_id WHERE b2.name = 'Mazda' AND c.model = 'MX-5 Miata');

INSERT INTO cars (brand_id, model, generation, body_type, description, image_url)
SELECT b.brand_id, 'M3', 'G80', 'Sedan',
       'The G80 M3 is powered by the S58 twin-turbocharged inline-six producing up to 503 hp in Competition trim. It was the first M3 offered with optional all-wheel drive and the first to spawn an M3 Touring variant.',
       'https://upload.wikimedia.org/wikipedia/commons/3/36/BMW_M3_%28G80%2C_2022%29_%2852227837026%29_%28cropped%29.jpg'
FROM brands b WHERE b.name = 'BMW' AND NOT EXISTS (SELECT 1 FROM cars c JOIN brands b2 ON c.brand_id = b2.brand_id WHERE b2.name = 'BMW' AND c.model = 'M3');

INSERT INTO cars (brand_id, model, generation, body_type, description, image_url)
SELECT b.brand_id, '911', '992', 'Coupe',
       'The 992-generation 911 is 45 mm wider than its predecessor and adopts an all-aluminium body structure. It retains the iconic rear-mounted flat-six engine while featuring electric pop-out door handles.',
       'https://upload.wikimedia.org/wikipedia/commons/b/b4/Porsche_911_992_2.jpg'
FROM brands b WHERE b.name = 'Porsche' AND NOT EXISTS (SELECT 1 FROM cars c JOIN brands b2 ON c.brand_id = b2.brand_id WHERE b2.name = 'Porsche' AND c.model = '911');

INSERT INTO cars (brand_id, model, generation, body_type, description, image_url)
SELECT b.brand_id, 'Civic Type R', 'FL5', 'Hatchback',
       'The FL5 Type R carries a 330 PS K20C1 turbocharged engine and introduces reshaped wider rear body panels. It features Honda''s LogR data logger for track use and a more refined design than its FK8 predecessor.',
       'https://upload.wikimedia.org/wikipedia/commons/4/4c/2022_Honda_Civic_Type_R_1.jpg'
FROM brands b WHERE b.name = 'Honda' AND NOT EXISTS (SELECT 1 FROM cars c JOIN brands b2 ON c.brand_id = b2.brand_id WHERE b2.name = 'Honda' AND c.model = 'Civic Type R');

INSERT INTO cars (brand_id, model, generation, body_type, description, image_url)
SELECT b.brand_id, 'WRX STI', 'VA', 'Sedan',
       'The VA-generation WRX STI was the first offered only as a sedan, powered by the EJ257 turbocharged flat-four producing 305 hp paired with a 6-speed manual and symmetrical all-wheel drive.',
       'https://upload.wikimedia.org/wikipedia/commons/c/c0/%2715_Subaru_WRX_STI_%28MIAS_%2715%29.jpg'
FROM brands b WHERE b.name = 'Subaru' AND NOT EXISTS (SELECT 1 FROM cars c JOIN brands b2 ON c.brand_id = b2.brand_id WHERE b2.name = 'Subaru' AND c.model = 'WRX STI');

INSERT INTO cars (brand_id, model, generation, body_type, description, image_url)
SELECT b.brand_id, 'GT-R', 'R35', 'Coupe',
       'The R35 GT-R was built around a hand-assembled VR38DETT twin-turbocharged V6, with each engine signed by one of nine certified Takumi craftsmen. Its combination of all-wheel drive and launch control made it a benchmark supercar-slayer.',
       'https://upload.wikimedia.org/wikipedia/commons/9/91/2009_Nissan_GT-R_Black_Edition_1.jpg'
FROM brands b WHERE b.name = 'Nissan' AND NOT EXISTS (SELECT 1 FROM cars c JOIN brands b2 ON c.brand_id = b2.brand_id WHERE b2.name = 'Nissan' AND c.model = 'GT-R');

INSERT INTO cars (brand_id, model, generation, body_type, description, image_url)
SELECT b.brand_id, 'Camaro', '6th Gen', 'Coupe',
       'Built on GM''s Alpha platform shared with the Cadillac ATS, the sixth-gen Camaro shed over 200 lbs versus its predecessor. Available engines ranged from a turbocharged 2.0L four-cylinder to a supercharged 6.2L V8 in the ZL1.',
       'https://upload.wikimedia.org/wikipedia/commons/4/41/2016-18_Chevrolet_Camaro_VI_rear%2C_7.20.19.jpg'
FROM brands b WHERE b.name = 'Chevrolet' AND NOT EXISTS (SELECT 1 FROM cars c JOIN brands b2 ON c.brand_id = b2.brand_id WHERE b2.name = 'Chevrolet' AND c.model = 'Camaro');

INSERT INTO cars (brand_id, model, generation, body_type, description, image_url)
SELECT b.brand_id, 'RS6 Avant', 'C8', 'Estate',
       'The C8 RS6 Avant is powered by a 4.0L twin-turbo V8 with a 48V mild-hybrid system producing 600 PS. It was the first RS6 Avant sold in North America since the C5 generation and the first RS6 to use a hybrid drivetrain.',
       'https://upload.wikimedia.org/wikipedia/commons/0/00/Audi_RS6_Avant_C8_1X7A0305.jpg'
FROM brands b WHERE b.name = 'Audi' AND NOT EXISTS (SELECT 1 FROM cars c JOIN brands b2 ON c.brand_id = b2.brand_id WHERE b2.name = 'Audi' AND c.model = 'RS6 Avant');

CREATE TABLE IF NOT EXISTS reviews (
    review_id        SERIAL PRIMARY KEY,
    user_id          INT          NOT NULL REFERENCES users(user_id),
    car_id           INT          NOT NULL REFERENCES cars(car_id),
    rating           NUMERIC(2,1) NOT NULL,
    title            VARCHAR(200) NOT NULL,
    body             TEXT         NOT NULL,
    ownership_status VARCHAR(20),
    model_year       INT,
    mileage_km       INT,
    would_recommend  BOOLEAN,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
