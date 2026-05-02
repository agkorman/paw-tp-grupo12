-- Local demo seed for showcasing the current feature set.
-- Run after schema.sql has been applied.
--
-- Demo login password for every user in this file: password
--   admin.demo@laposta.local      admin
--   driver.demo@laposta.local     user
--   eva.demo@laposta.local        user
--   marco.demo@laposta.local      user
--   lucia.demo@laposta.local      user

BEGIN;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'cars'
          AND column_name = 'year'
    ) THEN
        RAISE EXCEPTION 'local-demo-seed.sql requires the current schema. Run persistence/src/main/resources/schema.sql first.';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'cars'
          AND column_name = 'price_usd'
    ) THEN
        RAISE EXCEPTION 'local-demo-seed.sql requires the current schema. Run persistence/src/main/resources/schema.sql first.';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'review_replies'
    ) THEN
        RAISE EXCEPTION 'local-demo-seed.sql requires the current schema. Run persistence/src/main/resources/schema.sql first.';
    END IF;
END $$;

CREATE OR REPLACE FUNCTION pg_temp.demo_svg_image(
    p_title text,
    p_subtitle text,
    p_accent text,
    p_variant integer
) RETURNS bytea
LANGUAGE sql
AS $$
    SELECT convert_to(
        format(
$svg$<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 720" role="img" aria-label="%1$s %2$s">
<defs>
  <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
    <stop offset="0" stop-color="#111827"/>
    <stop offset="1" stop-color="#050816"/>
  </linearGradient>
  <linearGradient id="paint" x1="0" y1="0" x2="1" y2="0">
    <stop offset="0" stop-color="%3$s"/>
    <stop offset="1" stop-color="#e5e7eb"/>
  </linearGradient>
</defs>
<rect width="1200" height="720" fill="url(#bg)"/>
<rect x="0" y="500" width="1200" height="220" fill="#0f172a"/>
<circle cx="930" cy="130" r="170" fill="%3$s" opacity=".18"/>
<circle cx="240" cy="150" r="105" fill="#ffffff" opacity=".06"/>
<path d="M205 468 C286 358 381 320 536 320 L684 320 C784 320 867 365 940 468 Z" fill="url(#paint)"/>
<path d="M305 468 L392 376 L648 376 L738 468 Z" fill="#0b1020" opacity=".72"/>
<path d="M190 468 L1000 468 C1032 468 1058 494 1058 526 L1058 548 L132 548 L132 526 C132 494 158 468 190 468 Z" fill="%3$s"/>
<path d="M218 515 H974" stroke="#f8fafc" stroke-width="10" stroke-linecap="round" opacity=".58"/>
<circle cx="315" cy="548" r="70" fill="#020617"/>
<circle cx="315" cy="548" r="34" fill="#94a3b8"/>
<circle cx="875" cy="548" r="70" fill="#020617"/>
<circle cx="875" cy="548" r="34" fill="#94a3b8"/>
<text x="72" y="92" fill="#f8fafc" font-family="Inter, Arial, sans-serif" font-size="52" font-weight="800">%1$s</text>
<text x="74" y="142" fill="#cbd5e1" font-family="Inter, Arial, sans-serif" font-size="26">%2$s</text>
<text x="990" y="650" fill="#f8fafc" font-family="Inter, Arial, sans-serif" font-size="20" text-anchor="end" opacity=".72">Demo image %4$s</text>
</svg>$svg$,
            replace(replace(replace(coalesce(p_title, ''), '&', '&amp;'), '<', '&lt;'), '>', '&gt;'),
            replace(replace(replace(coalesce(p_subtitle, ''), '&', '&amp;'), '<', '&lt;'), '>', '&gt;'),
            coalesce(p_accent, '#38bdf8'),
            coalesce(p_variant, 1)
        ),
        'UTF8'
    );
$$;

INSERT INTO brands (name) VALUES
    ('Toyota'), ('Ford'), ('Mazda'), ('BMW'), ('Porsche'), ('Honda'), ('Subaru'),
    ('Nissan'), ('Chevrolet'), ('Audi'), ('Tesla'), ('Hyundai'), ('Kia'), ('Volvo'),
    ('Jeep'), ('RAM'), ('Volkswagen'), ('Mercedes-Benz'), ('Lexus'), ('Mini'),
    ('Alfa Romeo'), ('Land Rover'), ('Peugeot'), ('Renault'), ('Fiat'), ('Lotus'),
    ('Cadillac'), ('Rivian'), ('Polestar'), ('BYD')
ON CONFLICT (name) DO NOTHING;

INSERT INTO body_types (name) VALUES
    ('Coupe'), ('Sedan'), ('Roadster'), ('Hatchback'), ('Estate'), ('SUV'),
    ('Pickup'), ('Crossover'), ('Convertible'), ('Van')
ON CONFLICT (name) DO NOTHING;

INSERT INTO users (username, email, password, role) VALUES
    ('demo_admin', 'admin.demo@laposta.local', '$2a$10$rdoHKtTSqMiCADYlWBmBHeCdZgt4VXlw4QeNPHb9Aw8PCXjmI1Ib2', 'admin'),
    ('demo_driver', 'driver.demo@laposta.local', '$2a$10$rdoHKtTSqMiCADYlWBmBHeCdZgt4VXlw4QeNPHb9Aw8PCXjmI1Ib2', 'user'),
    ('demo_eva', 'eva.demo@laposta.local', '$2a$10$rdoHKtTSqMiCADYlWBmBHeCdZgt4VXlw4QeNPHb9Aw8PCXjmI1Ib2', 'user'),
    ('demo_marco', 'marco.demo@laposta.local', '$2a$10$rdoHKtTSqMiCADYlWBmBHeCdZgt4VXlw4QeNPHb9Aw8PCXjmI1Ib2', 'user'),
    ('demo_lucia', 'lucia.demo@laposta.local', '$2a$10$rdoHKtTSqMiCADYlWBmBHeCdZgt4VXlw4QeNPHb9Aw8PCXjmI1Ib2', 'user'),
    ('demo_santi', 'santi.demo@laposta.local', '$2a$10$rdoHKtTSqMiCADYlWBmBHeCdZgt4VXlw4QeNPHb9Aw8PCXjmI1Ib2', 'user'),
    ('demo_nadia', 'nadia.demo@laposta.local', '$2a$10$rdoHKtTSqMiCADYlWBmBHeCdZgt4VXlw4QeNPHb9Aw8PCXjmI1Ib2', 'user')
ON CONFLICT (email) DO UPDATE SET
    password = EXCLUDED.password,
    role = EXCLUDED.role;

CREATE TEMP TABLE demo_car_seed (
    brand_name text NOT NULL,
    model text NOT NULL,
    body_type_name text NOT NULL,
    model_year integer NOT NULL,
    description text NOT NULL,
    fuel_type text NOT NULL,
    horsepower integer NOT NULL,
    airbag_count integer NOT NULL,
    transmission text NOT NULL,
    fuel_consumption numeric(4,1) NOT NULL,
    max_speed_kmh integer NOT NULL,
    price_usd numeric(12,2) NOT NULL,
    accent text NOT NULL
) ON COMMIT DROP;

INSERT INTO demo_car_seed VALUES
    ('Toyota', 'GR Supra', 'Coupe', 2025, 'Coupe de seis cilindros con chasis compacto, buena respuesta de turbo y enfoque de manejo deportivo (model year 2025).', 'combustion', 382, 8, 'automatic', 9.8, 250, 49990, '#ef4444'),
    ('Toyota', 'GR Supra', 'Coupe', 2026, 'Coupe de seis cilindros con chasis compacto, buena respuesta de turbo y enfoque de manejo deportivo.', 'combustion', 382, 8, 'automatic', 9.8, 250, 51290, '#ef4444'),
    ('Ford', 'Mustang', 'Coupe', 2026, 'Muscle car moderno con V8 disponible, baul util y una puesta a punto mas precisa que generaciones anteriores.', 'combustion', 450, 6, 'automatic', 12.4, 250, 42995, '#f97316'),
    ('Mazda', 'MX-5 Miata', 'Roadster', 2026, 'Roadster liviano, simple y comunicativo, ideal para quien prioriza sensaciones antes que potencia bruta.', 'combustion', 184, 6, 'manual', 7.4, 214, 28050, '#dc2626'),
    ('BMW', 'M3', 'Sedan', 2026, 'Sedan de alto rendimiento con motor biturbo, mucha estabilidad y espacio suficiente para uso diario.', 'combustion', 503, 10, 'automatic', 10.5, 290, 76900, '#2563eb'),
    ('Porsche', '911', 'Coupe', 2026, 'El deportivo de referencia: rapido, preciso y suficientemente usable para viajes largos.', 'combustion', 385, 8, 'automatic', 10.2, 293, 106100, '#f59e0b'),
    ('Honda', 'Civic Type R', 'Hatchback', 2026, 'Hatchback manual de pista con baul util, direccion precisa y motor turbo muy aprovechable.', 'combustion', 330, 6, 'manual', 8.9, 272, 42895, '#ef4444'),
    ('Subaru', 'WRX STI', 'Sedan', 2026, 'Sedan con traccion integral, caja manual y caracter de rally para caminos exigentes.', 'combustion', 305, 6, 'manual', 10.7, 255, 39995, '#2563eb'),
    ('Nissan', 'GT-R', 'Coupe', 2026, 'Coupe de traccion integral con motor biturbo y aceleracion feroz.', 'combustion', 570, 6, 'automatic', 12.4, 315, 113540, '#94a3b8'),
    ('Chevrolet', 'Camaro', 'Coupe', 2026, 'Coupe americano con mucho empuje, chasis serio y presencia fuerte.', 'combustion', 650, 6, 'automatic', 14.7, 290, 62995, '#facc15'),
    ('Audi', 'RS6 Avant', 'Estate', 2026, 'Familiar de altas prestaciones con V8 hibrido suave, baul grande y traccion integral.', 'hybrid', 600, 10, 'automatic', 11.5, 280, 126895, '#22c55e'),
    ('Tesla', 'Model 3 Performance', 'Sedan', 2026, 'Sedan electrico de aceleracion inmediata, bajo costo de uso y buen alcance para ruta.', 'electric', 510, 8, 'automatic', 0.0, 261, 54990, '#38bdf8'),
    ('Tesla', 'Model Y Long Range', 'SUV', 2026, 'SUV electrico familiar con gran espacio interior, baul amplio y autonomia para uso mixto.', 'electric', 425, 8, 'automatic', 0.0, 217, 48990, '#0ea5e9'),
    ('Hyundai', 'Ioniq 5', 'Crossover', 2026, 'Crossover electrico de carga rapida, cabina amplia y andar muy confortable.', 'electric', 320, 7, 'automatic', 0.0, 185, 47800, '#14b8a6'),
    ('Kia', 'EV6 GT', 'Crossover', 2026, 'Crossover electrico de perfil deportivo, mucha potencia y buena estabilidad en ruta.', 'electric', 576, 8, 'automatic', 0.0, 260, 61900, '#a855f7'),
    ('Volvo', 'EX30', 'Crossover', 2026, 'Crossover electrico compacto con foco en seguridad, ciudad y tecnologia simple.', 'electric', 422, 7, 'automatic', 0.0, 180, 44900, '#64748b'),
    ('Toyota', 'Corolla Hybrid', 'Sedan', 2026, 'Sedan hibrido confiable, economico y facil de mantener.', 'hybrid', 138, 8, 'automatic', 4.7, 180, 24500, '#16a34a'),
    ('Toyota', 'RAV4 Hybrid', 'SUV', 2026, 'SUV hibrido equilibrado con buen espacio, consumo bajo y reputacion de confiabilidad.', 'hybrid', 219, 8, 'automatic', 5.8, 200, 34150, '#22c55e'),
    ('Ford', 'F-150 Lightning', 'Pickup', 2026, 'Pickup electrica con gran torque, caja de carga amplia y energia disponible para herramientas.', 'electric', 580, 8, 'automatic', 0.0, 180, 62995, '#60a5fa'),
    ('Jeep', 'Wrangler Rubicon', 'SUV', 2026, 'SUV todoterreno con ejes robustos, techo desmontable y mucha capacidad fuera del asfalto.', 'combustion', 285, 6, 'manual', 12.8, 180, 46995, '#84cc16'),
    ('RAM', '1500 Rebel', 'Pickup', 2026, 'Pickup grande con cabina comoda, buena capacidad de remolque y presencia off-road.', 'combustion', 395, 6, 'automatic', 13.1, 190, 57995, '#b45309'),
    ('Volkswagen', 'Golf GTI', 'Hatchback', 2026, 'Hatchback deportivo con equilibrio entre ciudad, ruta y manejo divertido.', 'combustion', 241, 7, 'automatic', 7.8, 250, 32995, '#ef4444'),
    ('Mercedes-Benz', 'C300', 'Sedan', 2026, 'Sedan premium con asistencia mild-hybrid, cabina silenciosa y buen confort diario.', 'hybrid', 255, 9, 'automatic', 7.1, 250, 48100, '#94a3b8'),
    ('Lexus', 'RX 500h', 'SUV', 2026, 'SUV hibrido premium con gran insonorizacion, seguridad y terminaciones cuidadas.', 'hybrid', 366, 10, 'automatic', 8.4, 210, 62900, '#334155'),
    ('Mini', 'Cooper S', 'Hatchback', 2026, 'Hatchback chico, agil y facil de estacionar, con caracter deportivo para ciudad.', 'combustion', 201, 6, 'manual', 6.9, 235, 32900, '#06b6d4'),
    ('Alfa Romeo', 'Giulia Quadrifoglio', 'Sedan', 2026, 'Sedan deportivo con V6 biturbo, direccion rapida y puesta a punto emocional.', 'combustion', 505, 8, 'automatic', 10.9, 307, 81900, '#dc2626'),
    ('Land Rover', 'Defender 110', 'SUV', 2026, 'SUV robusto con gran despeje, interior practico y capacidad real para aventura.', 'combustion', 395, 8, 'automatic', 11.2, 209, 67900, '#65a30d'),
    ('Peugeot', '208 GT', 'Hatchback', 2026, 'Hatchback urbano con buen equipamiento, consumo contenido y dimensiones comodas.', 'combustion', 130, 6, 'automatic', 6.1, 208, 26900, '#3b82f6'),
    ('Renault', 'Megane E-Tech', 'Hatchback', 2026, 'Hatchback electrico con baul practico, diseno moderno y manejo silencioso.', 'electric', 220, 7, 'automatic', 0.0, 160, 39900, '#facc15'),
    ('Fiat', 'Pulse Abarth', 'Crossover', 2026, 'Crossover compacto con motor turbo, despeje urbano y respuesta agil.', 'combustion', 175, 6, 'automatic', 7.4, 215, 28900, '#ef4444'),
    ('Porsche', 'Taycan 4S', 'Sedan', 2026, 'Sedan electrico de alto rendimiento con carga rapida, gran chasis y cabina premium.', 'electric', 522, 8, 'automatic', 0.0, 250, 111700, '#7c3aed'),
    ('Volkswagen', 'Gol', 'Hatchback', 1985, 'Gol G1: hatchback compacto con motor refrigerado por aire heredado del Beetle, mecanica simple y mucha presencia en Latinoamerica.', 'combustion', 54, 0, 'manual', 8.5, 145, 6500, '#1e3a8a'),
    ('Volkswagen', 'Gol', 'Hatchback', 1995, 'Gol G2 (carradura): rediseno con linea redondeada, mejor aerodinamica y motores AP a nafta o diesel.', 'combustion', 75, 0, 'manual', 7.8, 165, 8200, '#0ea5e9'),
    ('Volkswagen', 'Gol', 'Hatchback', 2005, 'Gol G3 con frente actualizado y motores 1.6 / 1.8: opcion economica de gran difusion en flotas y particulares.', 'combustion', 99, 2, 'manual', 7.4, 175, 11500, '#22c55e'),
    ('Volkswagen', 'Gol', 'Hatchback', 2015, 'Gol G6 (Gol Trend): plataforma renovada, mejor confort y seguridad activa basica para uso urbano.', 'combustion', 101, 2, 'manual', 6.8, 178, 14900, '#f59e0b'),
    ('Volkswagen', 'Gol', 'Hatchback', 2024, 'Gol G7 final: ultima evolucion del modelo antes de su discontinuacion, con equipamiento mejorado y motor 1.6 MSI.', 'combustion', 110, 4, 'manual', 6.5, 184, 17900, '#dc2626');

INSERT INTO cars (
    brand_id, model, body_type_id, year, description, fuel_type, horsepower,
    airbag_count, transmission, fuel_consumption, max_speed_kmh, price_usd
)
SELECT
    b.brand_id,
    d.model,
    bt.body_type_id,
    d.model_year,
    d.description,
    d.fuel_type,
    d.horsepower,
    d.airbag_count,
    d.transmission,
    d.fuel_consumption,
    d.max_speed_kmh,
    d.price_usd
FROM demo_car_seed d
JOIN brands b ON b.name = d.brand_name
JOIN body_types bt ON bt.name = d.body_type_name
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO UPDATE SET
    description = EXCLUDED.description,
    fuel_type = EXCLUDED.fuel_type,
    horsepower = EXCLUDED.horsepower,
    airbag_count = EXCLUDED.airbag_count,
    transmission = EXCLUDED.transmission,
    fuel_consumption = EXCLUDED.fuel_consumption,
    max_speed_kmh = EXCLUDED.max_speed_kmh,
    price_usd = EXCLUDED.price_usd;

CREATE TEMP TABLE demo_car_image_seed (
    brand_name text NOT NULL,
    model text NOT NULL,
    model_year integer NOT NULL,
    display_order integer NOT NULL,
    title text NOT NULL,
    subtitle text NOT NULL,
    accent text NOT NULL
) ON COMMIT DROP;

INSERT INTO demo_car_image_seed
SELECT brand_name, model, model_year, 0, brand_name || ' ' || model, body_type_name || ' demo cover', accent
FROM demo_car_seed;

INSERT INTO demo_car_image_seed VALUES
    ('Porsche', '911', 2026, 1, 'Porsche 911', 'rear three-quarter', '#f97316'),
    ('Porsche', '911', 2026, 2, 'Porsche 911', 'cockpit detail', '#f59e0b'),
    ('Porsche', '911', 2026, 3, 'Porsche 911', 'track stance', '#fb923c'),
    ('Tesla', 'Model 3 Performance', 2026, 1, 'Model 3 Performance', 'charging stop', '#38bdf8'),
    ('Tesla', 'Model 3 Performance', 2026, 2, 'Model 3 Performance', 'minimal cabin', '#0ea5e9'),
    ('Toyota', 'RAV4 Hybrid', 2026, 1, 'RAV4 Hybrid', 'family cargo', '#22c55e'),
    ('Toyota', 'RAV4 Hybrid', 2026, 2, 'RAV4 Hybrid', 'weekend route', '#84cc16'),
    ('Hyundai', 'Ioniq 5', 2026, 1, 'Ioniq 5', 'fast charging', '#14b8a6'),
    ('Hyundai', 'Ioniq 5', 2026, 2, 'Ioniq 5', 'lounge cabin', '#2dd4bf'),
    ('Ford', 'F-150 Lightning', 2026, 1, 'F-150 Lightning', 'bed utility', '#60a5fa'),
    ('Ford', 'F-150 Lightning', 2026, 2, 'F-150 Lightning', 'worksite power', '#2563eb');

INSERT INTO car_images (car_id, display_order, content_type, image_data, updated_at)
SELECT
    c.car_id,
    i.display_order,
    'image/svg+xml',
    pg_temp.demo_svg_image(i.title, i.subtitle, i.accent, i.display_order + 1),
    CURRENT_TIMESTAMP
FROM demo_car_image_seed i
JOIN brands b ON b.name = i.brand_name
JOIN cars c ON c.brand_id = b.brand_id AND c.model = i.model AND c.year = i.model_year
ON CONFLICT (car_id, display_order) DO UPDATE SET
    content_type = EXCLUDED.content_type,
    image_data = EXCLUDED.image_data,
    updated_at = EXCLUDED.updated_at;

CREATE TEMP TABLE demo_review_seed (
    reviewer_email text NOT NULL,
    brand_name text NOT NULL,
    model text NOT NULL,
    model_year integer NOT NULL,
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
    ('driver.demo@laposta.local', 'Porsche', '911', 2026, 5.0, 'Precision without drama', 'El auto entra en curva con una naturalidad impresionante y no exige pelearlo para ir rapido.', 'Propietario actual', 2026, 6200, true, CURRENT_TIMESTAMP - INTERVAL '2 days', ARRAY['agile_engine','good_for_highway','comfortable','good_resale']),
    ('eva.demo@laposta.local', 'Porsche', '911', 2026, 4.5, 'Daily usable sports car', 'Sorprende lo facil que es usarlo todos los dias. La suspension filtra bien y la caja hace todo simple.', 'Propietario actual', 2026, 9400, true, CURRENT_TIMESTAMP - INTERVAL '4 days', ARRAY['comfortable','good_for_city','agile_engine']),
    ('marco.demo@laposta.local', 'Porsche', '911', 2026, 4.0, 'Expensive but special', 'El mantenimiento no es barato, pero cada salida se siente especial y la calidad general es altisima.', 'Ex propietario', 2025, 18000, true, CURRENT_TIMESTAMP - INTERVAL '6 days', ARRAY['expensive_maintenance','agile_engine','good_resale']),
    ('lucia.demo@laposta.local', 'Porsche', '911', 2026, 5.0, 'Best steering feel', 'La direccion comunica todo y el tren delantero da muchisima confianza incluso en ruta mojada.', 'Propietario actual', 2026, 4100, true, CURRENT_TIMESTAMP - INTERVAL '8 days', ARRAY['agile_engine','safe','good_for_highway']),
    ('santi.demo@laposta.local', 'Porsche', '911', 2026, 4.5, 'Cabin is quiet on route', 'A velocidad constante es mas silencioso de lo esperado y los asientos no cansan.', 'Propietario actual', 2026, 12600, true, CURRENT_TIMESTAMP - INTERVAL '10 days', ARRAY['comfortable','good_for_highway']),
    ('nadia.demo@laposta.local', 'Porsche', '911', 2026, 4.0, 'Rear seats are symbolic', 'Adelante es perfecto, atras solo sirve para bolsos. El baul delantero ayuda pero no hace milagros.', 'Propietario actual', 2026, 7300, true, CURRENT_TIMESTAMP - INTERVAL '12 days', ARRAY['small_trunk','agile_engine']),
    ('driver.demo@laposta.local', 'Porsche', '911', 2026, 5.0, 'Still the benchmark', 'Probamos varios deportivos y este sigue siendo el punto medio perfecto entre rapidez y control.', 'Ex propietario', 2025, 22000, true, CURRENT_TIMESTAMP - INTERVAL '14 days', ARRAY['agile_engine','good_for_highway','good_resale']),
    ('eva.demo@laposta.local', 'Porsche', '911', 2026, 4.0, 'Fuel cost is real', 'Si se usa fuerte consume bastante y los neumaticos duran poco, pero la experiencia lo compensa.', 'Propietario actual', 2026, 15000, true, CURRENT_TIMESTAMP - INTERVAL '16 days', ARRAY['high_fuel_consumption','expensive_maintenance','agile_engine']),
    ('driver.demo@laposta.local', 'Tesla', 'Model 3 Performance', 2026, 4.5, 'Fast and cheap to run', 'La aceleracion es brutal y el costo por kilometro es bajisimo frente a un sedan naftero.', 'Propietario actual', 2026, 8600, true, CURRENT_TIMESTAMP - INTERVAL '3 days', ARRAY['low_fuel_consumption','agile_engine','good_for_city','safe']),
    ('marco.demo@laposta.local', 'Tesla', 'Model 3 Performance', 2026, 4.0, 'Great commuter', 'Para ciudad es muy comodo, silencioso y facil de estacionar. La pantalla central requiere acostumbrarse.', 'Propietario actual', 2026, 11200, true, CURRENT_TIMESTAMP - INTERVAL '7 days', ARRAY['good_for_city','easy_to_park','comfortable','poor_tech']),
    ('lucia.demo@laposta.local', 'Tesla', 'Model 3 Performance', 2026, 4.5, 'Strong road trip EV', 'Con planificacion de carga se viaja muy bien. La recuperacion de energia ayuda mucho en sierra.', 'Propietario actual', 2026, 19000, true, CURRENT_TIMESTAMP - INTERVAL '11 days', ARRAY['low_fuel_consumption','good_for_highway','comfortable']),
    ('eva.demo@laposta.local', 'Toyota', 'RAV4 Hybrid', 2026, 5.0, 'The sensible family pick', 'Consume poco, tiene mucho espacio y transmite la sensacion de que no va a romper nada.', 'Propietario actual', 2026, 14300, true, CURRENT_TIMESTAMP - INTERVAL '5 days', ARRAY['low_fuel_consumption','big_trunk','safe','good_first_car','cheap_maintenance']),
    ('santi.demo@laposta.local', 'Toyota', 'RAV4 Hybrid', 2026, 4.5, 'Relaxed on every trip', 'La caja e-CVT no entusiasma, pero el conjunto es comodisimo para familia y ruta.', 'Propietario actual', 2026, 20100, true, CURRENT_TIMESTAMP - INTERVAL '9 days', ARRAY['comfortable','good_for_highway','big_trunk','low_fuel_consumption']),
    ('nadia.demo@laposta.local', 'Toyota', 'RAV4 Hybrid', 2026, 4.5, 'Easy recommendation', 'No es el mas rapido, pero es seguro, espacioso y facil de revender.', 'Ex propietario', 2025, 35000, true, CURRENT_TIMESTAMP - INTERVAL '13 days', ARRAY['safe','good_resale','big_trunk','underpowered']),
    ('driver.demo@laposta.local', 'Toyota', 'Corolla Hybrid', 2026, 4.5, 'Low stress ownership', 'Gasta muy poco, los services son razonables y es muy facil de manejar.', 'Propietario actual', 2026, 9800, true, CURRENT_TIMESTAMP - INTERVAL '15 days', ARRAY['low_fuel_consumption','cheap_maintenance','good_first_car','easy_to_park']),
    ('lucia.demo@laposta.local', 'Toyota', 'Corolla Hybrid', 2026, 4.0, 'Efficient but calm', 'Ideal si buscas transporte confiable. En sobrepasos largos se nota que le falta potencia.', 'Propietario actual', 2026, 16500, true, CURRENT_TIMESTAMP - INTERVAL '22 days', ARRAY['low_fuel_consumption','underpowered','good_for_city']),
    ('marco.demo@laposta.local', 'Hyundai', 'Ioniq 5', 2026, 4.5, 'Comfort first EV', 'La posicion de manejo y el silencio de marcha son excelentes. El interior se siente amplio.', 'Propietario actual', 2026, 7200, true, CURRENT_TIMESTAMP - INTERVAL '18 days', ARRAY['comfortable','big_trunk','low_fuel_consumption','safe']),
    ('eva.demo@laposta.local', 'Hyundai', 'Ioniq 5', 2026, 4.0, 'Charging makes the difference', 'Cuando hay cargadores rapidos cerca es comodisimo. Para ciudad va sobrado.', 'Propietario actual', 2026, 11900, true, CURRENT_TIMESTAMP - INTERVAL '23 days', ARRAY['low_fuel_consumption','good_for_city','comfortable']),
    ('nadia.demo@laposta.local', 'Mazda', 'MX-5 Miata', 2026, 4.5, 'Pure driving joy', 'No hay otro auto nuevo que se sienta tan liviano y directo por este precio.', 'Propietario actual', 2026, 5400, true, CURRENT_TIMESTAMP - INTERVAL '19 days', ARRAY['agile_engine','easy_to_park','cheap_maintenance','small_trunk']),
    ('santi.demo@laposta.local', 'Mazda', 'MX-5 Miata', 2026, 3.5, 'Tiny but charming', 'El baul limita mucho y en autopista entra ruido, pero en curvas es una fiesta.', 'Ex propietario', 2025, 26000, true, CURRENT_TIMESTAMP - INTERVAL '25 days', ARRAY['small_trunk','noisy_cabin','agile_engine','bad_for_highway']),
    ('driver.demo@laposta.local', 'Honda', 'Civic Type R', 2026, 4.5, 'Track capable hatch', 'La caja manual es excelente y el motor empuja siempre. Suspensiones firmes para calles rotas.', 'Propietario actual', 2026, 8100, true, CURRENT_TIMESTAMP - INTERVAL '17 days', ARRAY['agile_engine','big_trunk','uncomfortable']),
    ('marco.demo@laposta.local', 'Honda', 'Civic Type R', 2026, 4.0, 'Practical speed', 'Tiene baul y plazas reales, aunque el ruido de rodadura se nota en ruta.', 'Propietario actual', 2026, 12800, true, CURRENT_TIMESTAMP - INTERVAL '21 days', ARRAY['agile_engine','big_trunk','noisy_cabin']),
    ('eva.demo@laposta.local', 'Ford', 'F-150 Lightning', 2026, 4.0, 'Useful electric truck', 'El torque instantaneo sorprende y usar la bateria para herramientas es muy practico.', 'Propietario actual', 2026, 9200, true, CURRENT_TIMESTAMP - INTERVAL '20 days', ARRAY['agile_engine','comfortable','safe']),
    ('lucia.demo@laposta.local', 'Jeep', 'Wrangler Rubicon', 2026, 3.5, 'Amazing off-road, tiring highway', 'Fuera del asfalto es excelente. En ruta es ruidoso y no es facil de estacionar.', 'Propietario actual', 2026, 17000, false, CURRENT_TIMESTAMP - INTERVAL '24 days', ARRAY['noisy_cabin','hard_to_park','bad_for_highway']),
    ('nadia.demo@laposta.local', 'Volkswagen', 'Golf GTI', 2026, 4.5, 'Best all-rounder hatch', 'Rapido, compacto y practico. Sirve para ir al trabajo y para disfrutar una ruta.', 'Propietario actual', 2026, 6900, true, CURRENT_TIMESTAMP - INTERVAL '26 days', ARRAY['agile_engine','good_for_city','good_for_highway','easy_to_park']);

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
JOIN cars c ON c.brand_id = b.brand_id AND c.model = d.model AND c.year = d.model_year
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
JOIN cars c ON c.brand_id = b.brand_id AND c.model = d.model AND c.year = d.model_year
JOIN reviews r ON r.user_id = u.user_id AND r.car_id = c.car_id AND r.title = d.title
CROSS JOIN LATERAL unnest(d.tag_codes) AS tag_code(code)
JOIN review_tags rt ON rt.code = tag_code.code
ON CONFLICT (review_id, tag_id) DO NOTHING;

CREATE TEMP TABLE demo_reply_seed (
    review_title text NOT NULL,
    author_email text NOT NULL,
    body text NOT NULL,
    created_at timestamptz NOT NULL
) ON COMMIT DROP;

INSERT INTO demo_reply_seed VALUES
    ('Precision without drama', 'eva.demo@laposta.local', 'Coincido, lo mas impresionante es que no intimida aunque vaya muy rapido.', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    ('Daily usable sports car', 'driver.demo@laposta.local', 'El modo normal ayuda mucho para ciudad. En Sport cambia completamente.', CURRENT_TIMESTAMP - INTERVAL '3 days'),
    ('Expensive but special', 'lucia.demo@laposta.local', 'Los costos son altos, pero la reventa compensa parte del golpe.', CURRENT_TIMESTAMP - INTERVAL '5 days'),
    ('Cabin is quiet on route', 'marco.demo@laposta.local', 'Con cubiertas touring mejora todavia mas el ruido de rodadura.', CURRENT_TIMESTAMP - INTERVAL '7 days'),
    ('Fast and cheap to run', 'nadia.demo@laposta.local', 'La diferencia de costo mensual frente a nafta se nota muchisimo.', CURRENT_TIMESTAMP - INTERVAL '2 days'),
    ('The sensible family pick', 'driver.demo@laposta.local', 'Es el auto que recomiendo cuando alguien no quiere pensar en mecanica.', CURRENT_TIMESTAMP - INTERVAL '4 days'),
    ('Pure driving joy', 'santi.demo@laposta.local', 'La caja manual es una de las mejores que probe.', CURRENT_TIMESTAMP - INTERVAL '6 days'),
    ('Amazing off-road, tiring highway', 'marco.demo@laposta.local', 'Para ruta larga conviene probarlo antes, el ruido puede cansar.', CURRENT_TIMESTAMP - INTERVAL '8 days');

INSERT INTO review_replies (review_id, user_id, body, created_at, updated_at)
SELECT
    r.review_id,
    u.user_id,
    d.body,
    d.created_at,
    d.created_at
FROM demo_reply_seed d
JOIN reviews r ON r.title = d.review_title
JOIN users u ON u.email = d.author_email
WHERE NOT EXISTS (
    SELECT 1
    FROM review_replies rr
    WHERE rr.review_id = r.review_id
      AND rr.user_id = u.user_id
      AND rr.body = d.body
);

CREATE TEMP TABLE demo_review_like_seed (
    review_title text NOT NULL,
    liker_email text NOT NULL
) ON COMMIT DROP;

INSERT INTO demo_review_like_seed VALUES
    ('Precision without drama', 'eva.demo@laposta.local'),
    ('Precision without drama', 'marco.demo@laposta.local'),
    ('Precision without drama', 'lucia.demo@laposta.local'),
    ('Daily usable sports car', 'driver.demo@laposta.local'),
    ('Daily usable sports car', 'nadia.demo@laposta.local'),
    ('Best steering feel', 'eva.demo@laposta.local'),
    ('Still the benchmark', 'lucia.demo@laposta.local'),
    ('Fast and cheap to run', 'marco.demo@laposta.local'),
    ('The sensible family pick', 'driver.demo@laposta.local'),
    ('The sensible family pick', 'lucia.demo@laposta.local'),
    ('Comfort first EV', 'eva.demo@laposta.local'),
    ('Pure driving joy', 'driver.demo@laposta.local'),
    ('Track capable hatch', 'marco.demo@laposta.local'),
    ('Best all-rounder hatch', 'eva.demo@laposta.local');

INSERT INTO review_likes (review_id, user_id, created_at)
SELECT r.review_id, u.user_id, CURRENT_TIMESTAMP
FROM demo_review_like_seed d
JOIN reviews r ON r.title = d.review_title
JOIN users u ON u.email = d.liker_email
ON CONFLICT (review_id, user_id) DO NOTHING;

INSERT INTO review_reply_likes (reply_id, user_id, created_at)
SELECT rr.reply_id, u.user_id, CURRENT_TIMESTAMP
FROM review_replies rr
JOIN users u ON u.email IN ('driver.demo@laposta.local', 'eva.demo@laposta.local')
WHERE rr.body IN (
    'Coincido, lo mas impresionante es que no intimida aunque vaya muy rapido.',
    'Es el auto que recomiendo cuando alguien no quiere pensar en mecanica.',
    'La caja manual es una de las mejores que probe.'
)
ON CONFLICT (reply_id, user_id) DO NOTHING;

INSERT INTO car_favorites (user_id, car_id, created_at)
SELECT u.user_id, c.car_id, CURRENT_TIMESTAMP
FROM (VALUES
    ('driver.demo@laposta.local', 'Porsche', '911', 2026),
    ('driver.demo@laposta.local', 'Toyota', 'RAV4 Hybrid', 2026),
    ('driver.demo@laposta.local', 'Tesla', 'Model 3 Performance', 2026),
    ('eva.demo@laposta.local', 'Toyota', 'Corolla Hybrid', 2026),
    ('eva.demo@laposta.local', 'Hyundai', 'Ioniq 5', 2026),
    ('marco.demo@laposta.local', 'Honda', 'Civic Type R', 2026),
    ('lucia.demo@laposta.local', 'Mazda', 'MX-5 Miata', 2026)
) AS d(email, brand_name, model, model_year)
JOIN users u ON u.email = d.email
JOIN brands b ON b.name = d.brand_name
JOIN cars c ON c.brand_id = b.brand_id AND c.model = d.model AND c.year = d.model_year
ON CONFLICT (user_id, car_id) DO NOTHING;

INSERT INTO user_follows (follower_id, followed_id, created_at)
SELECT follower.user_id, followed.user_id, CURRENT_TIMESTAMP
FROM (VALUES
    ('driver.demo@laposta.local', 'eva.demo@laposta.local'),
    ('driver.demo@laposta.local', 'marco.demo@laposta.local'),
    ('eva.demo@laposta.local', 'driver.demo@laposta.local'),
    ('marco.demo@laposta.local', 'lucia.demo@laposta.local'),
    ('lucia.demo@laposta.local', 'driver.demo@laposta.local'),
    ('nadia.demo@laposta.local', 'eva.demo@laposta.local')
) AS d(follower_email, followed_email)
JOIN users follower ON follower.email = d.follower_email
JOIN users followed ON followed.email = d.followed_email
ON CONFLICT (follower_id, followed_id) DO NOTHING;

CREATE TEMP TABLE demo_car_request_seed (
    submitter_email text NOT NULL,
    brand_name text NOT NULL,
    model text NOT NULL,
    body_type_name text NOT NULL,
    model_year integer NOT NULL,
    description text NOT NULL,
    fuel_type text NOT NULL,
    horsepower integer NOT NULL,
    airbag_count integer NOT NULL,
    transmission text NOT NULL,
    fuel_consumption numeric(4,1) NOT NULL,
    max_speed_kmh integer NOT NULL,
    price_usd numeric(12,2) NOT NULL,
    created_at timestamptz NOT NULL,
    accent text NOT NULL
) ON COMMIT DROP;

INSERT INTO demo_car_request_seed VALUES
    ('driver.demo@laposta.local', 'Lotus', 'Emira', 'Coupe', 2026, 'Solicitud demo con galeria: coupe liviano de motor central para revisar en admin.', 'combustion', 400, 6, 'manual', 9.9, 290, 99900, CURRENT_TIMESTAMP - INTERVAL '1 hour', '#f97316'),
    ('eva.demo@laposta.local', 'Cadillac', 'CT5-V Blackwing', 'Sedan', 2026, 'Sedan V8 manual pedido por la comunidad para comparar contra M3 y Giulia.', 'combustion', 668, 8, 'manual', 14.0, 322, 94990, CURRENT_TIMESTAMP - INTERVAL '2 hours', '#64748b'),
    ('marco.demo@laposta.local', 'Rivian', 'R1T', 'Pickup', 2026, 'Pickup electrica premium con cuatro motores y enfoque aventura.', 'electric', 835, 8, 'automatic', 0.0, 201, 73900, CURRENT_TIMESTAMP - INTERVAL '3 hours', '#22c55e'),
    ('lucia.demo@laposta.local', 'Polestar', '2', 'Sedan', 2026, 'Sedan electrico minimalista para ampliar la oferta de EV europeos.', 'electric', 455, 8, 'automatic', 0.0, 205, 49900, CURRENT_TIMESTAMP - INTERVAL '4 hours', '#38bdf8'),
    ('santi.demo@laposta.local', 'BYD', 'Seal', 'Sedan', 2026, 'Sedan electrico con buena autonomia y precio competitivo.', 'electric', 530, 8, 'automatic', 0.0, 240, 45900, CURRENT_TIMESTAMP - INTERVAL '5 hours', '#0ea5e9'),
    ('nadia.demo@laposta.local', 'Toyota', 'Land Cruiser', 'SUV', 2026, 'SUV robusto de larga trayectoria para usuarios que piden off-road confiable.', 'hybrid', 326, 8, 'automatic', 10.7, 190, 57900, CURRENT_TIMESTAMP - INTERVAL '6 hours', '#84cc16'),
    ('driver.demo@laposta.local', 'Ford', 'Bronco', 'SUV', 2026, 'Todoterreno moderno con techo removible y mucho interes en la comunidad.', 'combustion', 315, 6, 'automatic', 11.7, 190, 46900, CURRENT_TIMESTAMP - INTERVAL '7 hours', '#f59e0b'),
    ('eva.demo@laposta.local', 'Chevrolet', 'Corvette Stingray', 'Coupe', 2026, 'Deportivo de motor central solicitado para ampliar los coupes de alto rendimiento.', 'combustion', 495, 6, 'automatic', 12.3, 312, 69995, CURRENT_TIMESTAMP - INTERVAL '8 hours', '#ef4444'),
    ('marco.demo@laposta.local', 'Audi', 'Q6 e-tron', 'SUV', 2026, 'SUV electrico premium para contrastar contra Model Y e Ioniq 5.', 'electric', 456, 8, 'automatic', 0.0, 210, 65900, CURRENT_TIMESTAMP - INTERVAL '9 hours', '#60a5fa'),
    ('lucia.demo@laposta.local', 'BMW', 'i4 M50', 'Sedan', 2026, 'Sedan electrico deportivo con doble motor y enfoque rutero.', 'electric', 536, 8, 'automatic', 0.0, 225, 69900, CURRENT_TIMESTAMP - INTERVAL '10 hours', '#2563eb'),
    ('santi.demo@laposta.local', 'Mercedes-Benz', 'GLC 300', 'SUV', 2026, 'SUV premium hibrido suave para familias que buscan confort.', 'hybrid', 255, 9, 'automatic', 8.1, 240, 52900, CURRENT_TIMESTAMP - INTERVAL '11 hours', '#94a3b8'),
    ('nadia.demo@laposta.local', 'Nissan', 'Z', 'Coupe', 2026, 'Coupe biturbo de caja manual pedido por entusiastas.', 'combustion', 400, 6, 'manual', 10.8, 250, 42970, CURRENT_TIMESTAMP - INTERVAL '12 hours', '#facc15'),
    ('driver.demo@laposta.local', 'Subaru', 'BRZ', 'Coupe', 2026, 'Coupe liviano de traccion trasera para resenas de manejo puro.', 'combustion', 228, 6, 'manual', 8.4, 226, 31995, CURRENT_TIMESTAMP - INTERVAL '13 hours', '#3b82f6'),
    ('eva.demo@laposta.local', 'Hyundai', 'Santa Cruz', 'Pickup', 2026, 'Pickup compacta para usuarios urbanos que necesitan caja ocasional.', 'combustion', 281, 6, 'automatic', 10.2, 210, 39850, CURRENT_TIMESTAMP - INTERVAL '14 hours', '#14b8a6');

INSERT INTO car_requests (
    submitted_by_user_id, submitter_email, brand_id, body_type_id, year, model,
    description, image_content_type, image_data, status, created_at, fuel_type,
    horsepower, airbag_count, transmission, fuel_consumption, max_speed_kmh, price_usd
)
SELECT
    u.user_id,
    u.email,
    b.brand_id,
    bt.body_type_id,
    d.model_year,
    d.model,
    d.description,
    NULL,
    NULL,
    'pending',
    d.created_at,
    d.fuel_type,
    d.horsepower,
    d.airbag_count,
    d.transmission,
    d.fuel_consumption,
    d.max_speed_kmh,
    d.price_usd
FROM demo_car_request_seed d
JOIN users u ON u.email = d.submitter_email
JOIN brands b ON b.name = d.brand_name
JOIN body_types bt ON bt.name = d.body_type_name
WHERE NOT EXISTS (
    SELECT 1
    FROM car_requests cr
    WHERE LOWER(cr.submitter_email) = LOWER(d.submitter_email)
      AND cr.model = d.model
);

CREATE TEMP TABLE demo_request_image_seed (
    submitter_email text NOT NULL,
    model text NOT NULL,
    display_order integer NOT NULL,
    title text NOT NULL,
    subtitle text NOT NULL,
    accent text NOT NULL
) ON COMMIT DROP;

INSERT INTO demo_request_image_seed
SELECT submitter_email, model, 0, brand_name || ' ' || model, 'pending request cover', accent
FROM demo_car_request_seed;

INSERT INTO demo_request_image_seed VALUES
    ('driver.demo@laposta.local', 'Emira', 1, 'Lotus Emira', 'interior request image', '#fb923c'),
    ('driver.demo@laposta.local', 'Emira', 2, 'Lotus Emira', 'rear request image', '#f97316'),
    ('marco.demo@laposta.local', 'R1T', 1, 'Rivian R1T', 'adventure request image', '#22c55e'),
    ('lucia.demo@laposta.local', '2', 1, 'Polestar 2', 'charging request image', '#38bdf8');

INSERT INTO car_request_images (car_request_id, display_order, content_type, image_data, updated_at)
SELECT
    cr.car_request_id,
    i.display_order,
    'image/svg+xml',
    pg_temp.demo_svg_image(i.title, i.subtitle, i.accent, i.display_order + 1),
    CURRENT_TIMESTAMP
FROM demo_request_image_seed i
JOIN car_requests cr ON LOWER(cr.submitter_email) = LOWER(i.submitter_email) AND cr.model = i.model
ON CONFLICT (car_request_id, display_order) DO UPDATE SET
    content_type = EXCLUDED.content_type,
    image_data = EXCLUDED.image_data,
    updated_at = EXCLUDED.updated_at;

INSERT INTO brand_requests (submitted_by_user_id, submitter_email, name, comments, status, created_at)
SELECT u.user_id, u.email, d.name, d.comments, 'pending', d.created_at
FROM (VALUES
    ('driver.demo@laposta.local', 'Genesis', 'Falta Genesis para sumar sedanes y SUVs premium.', CURRENT_TIMESTAMP - INTERVAL '2 days'),
    ('eva.demo@laposta.local', 'Cupra', 'Marca pedida para modelos deportivos compactos.', CURRENT_TIMESTAMP - INTERVAL '3 days'),
    ('marco.demo@laposta.local', 'Dodge', 'Necesaria para Charger y Challenger usados.', CURRENT_TIMESTAMP - INTERVAL '4 days'),
    ('lucia.demo@laposta.local', 'Mitsubishi', 'Para sumar Montero y Outlander.', CURRENT_TIMESTAMP - INTERVAL '5 days')
) AS d(email, name, comments, created_at)
JOIN users u ON u.email = d.email
WHERE NOT EXISTS (
    SELECT 1 FROM brand_requests br
    WHERE LOWER(br.submitter_email) = LOWER(d.email)
      AND LOWER(br.name) = LOWER(d.name)
);

INSERT INTO body_type_requests (submitted_by_user_id, submitter_email, name, comments, status, created_at)
SELECT u.user_id, u.email, d.name, d.comments, 'pending', d.created_at
FROM (VALUES
    ('driver.demo@laposta.local', 'Liftback', 'Para modelos tipo fastback con porton trasero.', CURRENT_TIMESTAMP - INTERVAL '2 days'),
    ('eva.demo@laposta.local', 'Shooting Brake', 'Carroceria interesante para familiares deportivas.', CURRENT_TIMESTAMP - INTERVAL '3 days'),
    ('marco.demo@laposta.local', 'Microcar', 'Para autos urbanos muy chicos.', CURRENT_TIMESTAMP - INTERVAL '4 days'),
    ('lucia.demo@laposta.local', 'Targa', 'Para deportivos con techo removible parcial.', CURRENT_TIMESTAMP - INTERVAL '5 days')
) AS d(email, name, comments, created_at)
JOIN users u ON u.email = d.email
WHERE NOT EXISTS (
    SELECT 1 FROM body_type_requests btr
    WHERE LOWER(btr.submitter_email) = LOWER(d.email)
      AND LOWER(btr.name) = LOWER(d.name)
);

INSERT INTO admin_requests (
    submitted_by_user_id, submitter_email, motivation, bio, justification, status, created_at
)
SELECT u.user_id, u.email, d.motivation, d.bio, d.justification, 'pending', d.created_at
FROM (VALUES
    ('eva.demo@laposta.local', 'Quiero ayudar a moderar altas de autos electricos.', 'Uso EV a diario y sigo lanzamientos regionales.', 'Puedo revisar fichas tecnicas y detectar datos inconsistentes.', CURRENT_TIMESTAMP - INTERVAL '6 days'),
    ('marco.demo@laposta.local', 'Me interesa ordenar solicitudes de deportivos.', 'Participo en track days y pruebo autos de amigos.', 'Puedo validar potencia, versiones y anios de modelos deportivos.', CURRENT_TIMESTAMP - INTERVAL '7 days'),
    ('lucia.demo@laposta.local', 'Quiero colaborar con resenas de seguridad familiar.', 'Manejo con chicos y priorizo seguridad, baul y consumo.', 'Puedo revisar que las solicitudes tengan informacion util para familias.', CURRENT_TIMESTAMP - INTERVAL '8 days'),
    ('santi.demo@laposta.local', 'Puedo ayudar con pickups y todoterrenos.', 'Trabajo con vehiculos de carga y rutas de ripio.', 'Conozco capacidades, consumos reales y versiones off-road.', CURRENT_TIMESTAMP - INTERVAL '9 days')
) AS d(email, motivation, bio, justification, created_at)
JOIN users u ON u.email = d.email
WHERE NOT EXISTS (
    SELECT 1 FROM admin_requests ar
    WHERE ar.submitted_by_user_id = u.user_id
      AND ar.motivation = d.motivation
);

COMMIT;
