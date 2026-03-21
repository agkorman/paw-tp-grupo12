CREATE TABLE IF NOT EXISTS cars (
    car_id      SERIAL PRIMARY KEY,
    brand       VARCHAR(50)  NOT NULL,
    model       VARCHAR(50)  NOT NULL,
    generation  VARCHAR(50),
    description TEXT,
    image_url   VARCHAR(500)
);

INSERT INTO cars (brand, model, generation, description, image_url)
SELECT 'Ford', 'Bronco Sport', '2021', 'Compact SUV with off-road capability and modern styling.', '/images/Bronco_Sport.jpg'
WHERE NOT EXISTS (SELECT 1 FROM cars WHERE brand = 'Ford' AND model = 'Bronco Sport');

INSERT INTO cars (brand, model, generation, description, image_url)
SELECT 'Toyota', 'Supra', 'A90', 'Iconic sports car revived with a turbocharged inline-six engine.', NULL
WHERE NOT EXISTS (SELECT 1 FROM cars WHERE brand = 'Toyota' AND model = 'Supra');

INSERT INTO cars (brand, model, generation, description, image_url)
SELECT 'Mazda', 'MX-5 Miata', 'ND', 'Lightweight roadster celebrated for its pure driving experience.', NULL
WHERE NOT EXISTS (SELECT 1 FROM cars WHERE brand = 'Mazda' AND model = 'MX-5 Miata');
