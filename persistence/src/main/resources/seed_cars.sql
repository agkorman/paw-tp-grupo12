-- ============================================================
-- SEED DATA — full car catalog with year variants
-- Run AFTER schema.sql
-- All filenames match download_seed_images.sh output
-- ============================================================

-- Additional body types
INSERT INTO body_types (name) VALUES
  ('SUV'), ('Pickup'), ('Crossover'), ('Convertible'), ('Minivan')
ON CONFLICT (name) DO NOTHING;

-- ─────────────────────────────────────────────────────────
-- TOYOTA
-- ─────────────────────────────────────────────────────────

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Toyota'),
  'Corolla',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2014,
  'La décima generación del sedán más vendido del mundo, con el motor 2ZR-FE 1.8 y transmisión CVT. Sólido, aburrido y confiable como un clavo: exactamente lo que espera su comprador.',
  'combustion', 132, 6, 'automatic',
  7.1, 185, 17800.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Toyota'),
  'Corolla',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2019,
  'La undécima generación E210 con la plataforma TNGA-C marca un salto en dinámica de manejo respecto al modelo anterior. El 2.0 Dynamic Force de 169 HP le aporta algo de carácter al sedán compacto más vendido del planeta.',
  'combustion', 169, 7, 'automatic',
  6.3, 200, 20300.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Toyota'),
  'Corolla',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2023,
  'El Corolla híbrido E210 con el sistema de segunda generación ofrece 3.4 L/100 km, uno de los consumos más bajos del segmento. La carrocería no cambia pero el tren motriz es sensiblemente más refinado.',
  'hybrid', 122, 7, 'automatic',
  3.4, 180, 23600.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Toyota'),
  'Hilux',
  (SELECT body_type_id FROM body_types WHERE name = 'Pickup'),
  2016,
  'La octava generación de la pick-up más vendida de la Argentina. El 2.8 turbodiesel GD debuta en esta generación con 177 HP y un torque de 420 Nm, fabricada en la planta de Zárate.',
  'combustion', 177, 6, 'manual',
  8.5, 170, 29900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Toyota'),
  'Hilux',
  (SELECT body_type_id FROM body_types WHERE name = 'Pickup'),
  2021,
  'El facelift 2021 de la Hilux octava generación suma 27 HP al motor 2.8 GD en versiones automáticas, alcanzando los 204 HP y 500 Nm. La versión GR-Sport agrega look Dakar sin comprometer la capacidad off-road.',
  'combustion', 204, 7, 'automatic',
  8.1, 175, 38500.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Toyota'),
  'RAV4',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2016,
  'La cuarta generación del RAV4 con el motor 2AR-FE 2.5 atmosférico. Familiar, espacioso y sin grandes pretensiones: el SUV de la clase media argentina que elegía Toyota antes de que existiera el Tracker.',
  'combustion', 176, 6, 'automatic',
  7.6, 180, 26500.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Toyota'),
  'RAV4',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2021,
  'La quinta generación XA50 llegó con plataforma TNGA-K y sistema híbrido AWD de 219 HP con motor eléctrico en el eje trasero. Combina tracción inteligente y consumo de auto chico para un SUV familiar.',
  'hybrid', 219, 8, 'automatic',
  5.7, 180, 33900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Toyota'),
  'Land Cruiser',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2015,
  'El J200 con el 4.6 V8 biturbo diesel de 309 HP: el SUV tanque preferido por agencias de Naciones Unidas, estancieros y quienes cruzan el Sahara en sus vacaciones. Indestructible y con un consumo acorde a su tamaño.',
  'combustion', 309, 8, 'automatic',
  13.5, 210, 65900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Toyota'),
  'Land Cruiser',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2022,
  'El J300 reemplazó al V8 histórico por un V6 biturbo de 3.5L más eficiente pero igualmente poderoso. Con 409 HP, estructura de nuevo chasis y tecnología de asistencia off-road de última generación.',
  'combustion', 409, 10, 'automatic',
  12.4, 210, 85900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Toyota'),
  'Yaris',
  (SELECT body_type_id FROM body_types WHERE name = 'Hatchback'),
  2018,
  'La tercera generación del Yaris fabricado en Brasil con el 1.5 1NZ-FE atmosférico. Chico, liviano y económico de mantener: el hatchback urbano por excelencia en el mercado argentino.',
  'combustion', 107, 6, 'automatic',
  6.5, 175, 16200.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Toyota'),
  'Yaris',
  (SELECT body_type_id FROM body_types WHERE name = 'Hatchback'),
  2023,
  'El facelift del Yaris XP150 incorpora el 1.5 M15A-FKS de ciclo Atkinson con 120 HP y mayor eficiencia. Mantiene el DNA de auto práctico y agrega conectividad Apple CarPlay/Android Auto de serie.',
  'combustion', 120, 7, 'automatic',
  5.8, 175, 19800.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Toyota'),
  'Prius',
  (SELECT body_type_id FROM body_types WHERE name = 'Hatchback'),
  2016,
  'La cuarta generación XW50 con la nueva plataforma TNGA debutó con un coeficiente aerodinámico de 0.24 Cd. El sistema Hybrid Synergy Drive de segunda generación ofrece consumos reales cercanos a 4.5 L/100 km.',
  'hybrid', 121, 7, 'automatic',
  3.9, 180, 25900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Toyota'),
  'Prius',
  (SELECT body_type_id FROM body_types WHERE name = 'Hatchback'),
  2023,
  'La quinta generación del Prius es irreconociblemente sexy comparada con sus ancestros: líneas bajas, llantas enormes y el nuevo sistema PHEV opcional. El 2.0 con motor eléctrico totaliza 194 HP.',
  'hybrid', 194, 7, 'automatic',
  3.7, 180, 28900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Toyota'),
  'GR86',
  (SELECT body_type_id FROM body_types WHERE name = 'Coupe'),
  2022,
  'La segunda generación del coupé deportivo Toyota-Subaru reemplaza el 2.0 boxer por un 2.4 FA24 de 234 HP. Sin turbos, sin tracción integral, sin excusas: propulsión trasera y caja de seis velocidades.',
  'combustion', 234, 6, 'manual',
  9.5, 226, 28400.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

-- ─────────────────────────────────────────────────────────
-- FORD
-- ─────────────────────────────────────────────────────────

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Ford'),
  'F-150',
  (SELECT body_type_id FROM body_types WHERE name = 'Pickup'),
  2015,
  'La 13ª generación de la pick-up más vendida de EE.UU. estrenó carrocería de aluminio militar que redujo 320 kg de peso. El 2.7 EcoBoost V6 biturbo debutó aquí como alternativa al V8, combinando potencia y eficiencia.',
  'combustion', 365, 6, 'automatic',
  12.5, 180, 31900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Ford'),
  'F-150',
  (SELECT body_type_id FROM body_types WHERE name = 'Pickup'),
  2021,
  'La 14ª generación refina la fórmula de aluminio con nuevo interior, pantalla central de hasta 12 pulgadas y el V6 PowerBoost híbrido enchufable de 430 HP. La versión Lightning eléctrica se anunció en este año.',
  'combustion', 400, 6, 'automatic',
  11.8, 180, 39900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Ford'),
  'Ranger',
  (SELECT body_type_id FROM body_types WHERE name = 'Pickup'),
  2017,
  'La quinta generación del Ranger T6, fabricada en General Pacheco, llegó al mercado con el 2.2 TDCi turbodiesel de 170 HP. Durante años fue el auto más vendido de Argentina en cualquier segmento.',
  'combustion', 170, 6, 'manual',
  8.3, 175, 28500.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Ford'),
  'Ranger',
  (SELECT body_type_id FROM body_types WHERE name = 'Pickup'),
  2023,
  'La generación T6.2 del Ranger renovó plataforma, conectividad y diseño exterior manteniendo los motores probados. La versión Raptor con el V6 EcoBoost de 3.0L sube a 397 HP para dominar el off-road.',
  'combustion', 170, 7, 'automatic',
  8.0, 175, 35800.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Ford'),
  'Bronco',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2021,
  'El regreso del Bronco después de 25 años ausencia: 4x4 puro con techo desmontable, puertas removibles y modo G.O.A.T. con 7 configuraciones de tracción. El 2.7 EcoBoost de 330 HP le da músculo real.',
  'combustion', 330, 8, 'automatic',
  12.5, 180, 34000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Ford'),
  'Fiesta',
  (SELECT body_type_id FROM body_types WHERE name = 'Hatchback'),
  2016,
  'La séptima generación del hatchback chico de Ford con el 1.6 Ti-VCT de 100 HP. Pequeño, ágil y con una mano de manejo que sus rivales japoneses no igualan; la versión ST con el 1.5 EcoBoost es otro asunto.',
  'combustion', 100, 6, 'automatic',
  6.0, 175, 14500.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Ford'),
  'Fiesta',
  (SELECT body_type_id FROM body_types WHERE name = 'Hatchback'),
  2019,
  'El Fiesta ST de séptima generación con el 1.5 EcoBoost de tres cilindros y 200 HP es uno de los mejores hot hatches de todos los tiempos. Diferencial de deslizamiento limitado Quaife de serie y una caja de seis que es pura manteca.',
  'combustion', 200, 7, 'manual',
  6.0, 232, 24900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Ford'),
  'Focus',
  (SELECT body_type_id FROM body_types WHERE name = 'Hatchback'),
  2016,
  'La cuarta generación del Focus con el 1.5 EcoBoost de 182 HP y la transmisión PowerShift de doble embrague. Referente de manejo en su segmento, aunque la caja robotizada tiene una reputación controvertida.',
  'combustion', 182, 7, 'automatic',
  6.3, 220, 19500.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Ford'),
  'Focus',
  (SELECT body_type_id FROM body_types WHERE name = 'Hatchback'),
  2019,
  'El Focus ST de cuarta generación con el 2.3 EcoBoost de 280 HP hereda el motor del Mustang EcoBoost. Suspensión adaptativa, modos de conducción y la dinámica de siempre, ahora con mucho más músculo bajo el capot.',
  'combustion', 280, 7, 'manual',
  7.8, 272, 38500.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Ford'),
  'GT',
  (SELECT body_type_id FROM body_types WHERE name = 'Coupe'),
  2017,
  'El superdeportivo americano en homenaje al GT40 ganador de Le Mans. Solo 1.350 unidades en cuatro años; carrocería monocasco de fibra de carbono y el V6 EcoBoost 3.5 biturbo de 660 HP que supera a muchos V12 europeos.',
  'combustion', 660, 6, 'automatic',
  14.7, 347, 450000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Ford'),
  'Maverick',
  (SELECT body_type_id FROM body_types WHERE name = 'Pickup'),
  2022,
  'La pick-up compacta híbrida que abrió un nicho nuevo en 2022. Con el 2.5 e-CVT de 191 HP combinados, consume menos que muchos sedanes. Fabricada en México, fue furor de ventas en EE.UU. desde el primer día.',
  'hybrid', 191, 7, 'automatic',
  5.9, 175, 22900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

-- ─────────────────────────────────────────────────────────
-- MAZDA
-- ─────────────────────────────────────────────────────────

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Mazda'),
  'RX-7',
  (SELECT body_type_id FROM body_types WHERE name = 'Coupe'),
  1999,
  'La generación FD3S (1992-2002) del RX-7 es el último gran rotativo biturbo secuencial de Mazda, con 280 HP declarados por el Acuerdo de Caballeros japonés. Protagonista del manga Initial D y de Tokyo Drift, ahora piezas escasas.',
  'combustion', 280, 2, 'manual',
  11.1, 250, NULL
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Mazda'),
  'RX-8',
  (SELECT body_type_id FROM body_types WHERE name = 'Coupe'),
  2004,
  'El RX-8 llegó en 2003 con el Renesis de 1.3 atmosférico que revaba hasta 9.000 RPM entregando 232 HP. Sus puertas suicidas atrás hacían posible acomodar cuatro pasajeros en un coupé deportivo. Ya no se hace más.',
  'combustion', 232, 6, 'manual',
  11.5, 234, NULL
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Mazda'),
  'RX-8',
  (SELECT body_type_id FROM body_types WHERE name = 'Coupe'),
  2009,
  'El facelift del RX-8 de 2009 mejoró el sistema de inyección para reducir el consumo de aceite, el talón de Aquiles histórico del rotativo. El motor RENESIS sigue siendo una rareza mecánica irrepetible.',
  'combustion', 232, 6, 'manual',
  11.5, 234, NULL
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Mazda'),
  'Mazda3',
  (SELECT body_type_id FROM body_types WHERE name = 'Hatchback'),
  2014,
  'La tercera generación del Mazda3 (BM) inauguró el lenguaje de diseño Kodo con su carrocería esculpida. El 2.0 SkyActiv-G de 155 HP y el SkyActiv-Drive lo convirtieron en el compacto más elegante del segmento.',
  'combustion', 155, 6, 'automatic',
  6.4, 200, 21500.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Mazda'),
  'Mazda3',
  (SELECT body_type_id FROM body_types WHERE name = 'Hatchback'),
  2019,
  'La cuarta generación (BP) elevó el Kodo a su máxima expresión con interior quasi-premium y el nuevo 2.5 SkyActiv-G de 186 HP. La versión Turbo de 250 HP convirtió al Mazda3 en un sleeper deportivo para entendidos.',
  'combustion', 186, 7, 'automatic',
  6.2, 220, 27900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Mazda'),
  'Mazda6',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2014,
  'La tercera generación GJ del Mazda6 con el 2.5 SkyActiv-G demostró que el diseño japonés puede competir con el estilo alemán. Interior de cuero, pantalla central y dinámica de manejo que avergüenza a rivales más caros.',
  'combustion', 192, 6, 'automatic',
  6.8, 220, 26900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Mazda'),
  'Mazda6',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2019,
  'El facelift 2019 del Mazda6 incorporó el 2.5T turbo de 231 HP con par de 420 Nm, un salto cualitativo notable. El interior recibió materiales Nappa y madera japonesa Washi que compiten con el segmento premium.',
  'combustion', 231, 6, 'automatic',
  8.5, 224, 34500.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Mazda'),
  'CX-5',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2017,
  'La segunda generación (KF) del SUV más vendido de Mazda a nivel mundial introdujo el 2.5 SkyActiv-G con cilindros desactivables. La combinación de diseño premium y precio accesible lo convirtió en líder de segmento.',
  'combustion', 187, 6, 'automatic',
  8.5, 200, 27500.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Mazda'),
  'CX-5',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2022,
  'El facelift 2022 del CX-5 KF incorpora el 2.5T turbo de 227 HP (256 con nafta premium) y actualiza el interior al estilo del CX-50. Sigue siendo el referente de manejo deportivo en el segmento SUV compacto.',
  'combustion', 227, 6, 'automatic',
  8.5, 207, 36900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Mazda'),
  'CX-30',
  (SELECT body_type_id FROM body_types WHERE name = 'Crossover'),
  2020,
  'El crossover que llena el hueco entre CX-3 y CX-5 con la plataforma del Mazda3. El 2.0 SkyActiv-X de 180 HP con ignición por compresión controlada por chispa es una rareza tecnológica que no llegó a todos los mercados.',
  'combustion', 122, 7, 'automatic',
  6.4, 186, 25900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Mazda'),
  'BT-50',
  (SELECT body_type_id FROM body_types WHERE name = 'Pickup'),
  2020,
  'La segunda generación del BT-50 llegó sobre la plataforma de la Isuzu D-Max con el 1.9 turbodiesel de 150 HP y el 3.0 de 190 HP. Menos conocida que sus rivales pero igual de capaz para el trabajo pesado.',
  'combustion', 170, 6, 'automatic',
  8.0, 175, 32500.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

-- ─────────────────────────────────────────────────────────
-- BMW
-- ─────────────────────────────────────────────────────────

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'BMW'),
  'M5',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2018,
  'El F90 fue el primer M5 de la historia con tracción integral M xDrive. El S63 V8 biturbo de 4.4L entrega 600 HP en versión base y 625 en Competition. Un misil de 4 puertas que aún puede hacer el kart si se le pide.',
  'combustion', 600, 8, 'automatic',
  10.7, 305, 105000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'BMW'),
  'M5',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2022,
  'El facelift F90 Competition de 2022 sube a 625 HP y estrena nuevos faros LED Matrix. El M xDrive permite desconectar el eje delantero para drifts perfectamente controlados. El último M5 antes de la electrificación.',
  'combustion', 625, 8, 'automatic',
  10.7, 305, 117000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'BMW'),
  '5 Series',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2017,
  'El G30 de 2017 llevó el Série 5 a la plataforma CLAR, ganando 100 kg respecto al F10. El 530i con el B46 2.0 turbo de 252 HP es el equilibrio ideal entre consumo y prestaciones en el segmento ejecutivo.',
  'combustion', 252, 8, 'automatic',
  6.4, 250, 55000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'BMW'),
  '5 Series',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2021,
  'El 530e G30 facelift con el sistema iPerformance de 292 HP combina el B46 2.0 turbo con motor eléctrico y 18 km de autonomía eléctrica. La solución ideal para quienes manejan ciudad y autovía por igual.',
  'hybrid', 292, 8, 'automatic',
  2.1, 250, 68000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'BMW'),
  'X3',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2018,
  'La tercera generación G01 del X3 sobre la plataforma CLAR ganó espacio y tecnología. El xDrive30i con el B46 2.0 turbo de 248 HP equilibra consumo y desempeño en el SUV compacto premium de mayor venta de BMW.',
  'combustion', 248, 8, 'automatic',
  8.1, 240, 52000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'BMW'),
  'X3',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2022,
  'El X3 M Competition F97 con el S58 biturbo de 510 HP convierte al SUV familiar en una bestia de pista. El facelift 2022 actualiza tecnología y adelanta el lenguaje del G01 renovado. 0-100 en 3.8 segundos.',
  'combustion', 510, 8, 'automatic',
  10.6, 285, 74000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'BMW'),
  'X5',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2019,
  'La cuarta generación G05 del X5, más grande y tecnológica que nunca. El xDrive40i con el B58 3.0 inline-six de 340 HP es el punto dulce: potencia suficiente, consumo razonable y el lujo interior que espera el comprador premium.',
  'combustion', 340, 8, 'automatic',
  8.5, 243, 73000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'BMW'),
  'X5',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2023,
  'El X5 xDrive50e G05 con el sistema PHEV de 394 HP y 80 km de autonomía eléctrica demuestra que el SUV premium puede ser eficiente. La batería de 25.7 kWh se carga en menos de 3 horas en wall box.',
  'hybrid', 394, 8, 'automatic',
  2.8, 250, 93000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'BMW'),
  'Z4',
  (SELECT body_type_id FROM body_types WHERE name = 'Roadster'),
  2019,
  'El G29 Z4 M40i con el B58 de 382 HP comparte plataforma con el Toyota GR Supra, proyecto conjunto entre ambas marcas. Techo de tela eléctrico que se pliega en 10 segundos: el roadster que BMW necesitaba volver a hacer.',
  'combustion', 382, 6, 'automatic',
  7.4, 250, 65500.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'BMW'),
  '1 Series',
  (SELECT body_type_id FROM body_types WHERE name = 'Hatchback'),
  2020,
  'El F40 es el primer Serie 1 con tracción delantera, lo que encendió el debate entre puristas. La versión M135i xDrive con el B48 2.0 turbo de 306 HP responde con 5.1 segundos de 0-100 y la misma diversión de siempre.',
  'combustion', 306, 7, 'automatic',
  7.5, 250, 44000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'BMW'),
  'i4',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2022,
  'El primer sedán deportivo 100% eléctrico de BMW comparte plataforma con el Serie 4. La versión M50 con motor dual entrega 544 HP y acelera de 0-100 en 3.9 segundos. Autonomía WLTP de hasta 510 km.',
  'electric', 544, 8, 'automatic',
  0.0, 225, 72000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

-- ─────────────────────────────────────────────────────────
-- PORSCHE
-- ─────────────────────────────────────────────────────────

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Porsche'),
  '718 Cayman',
  (SELECT body_type_id FROM body_types WHERE name = 'Coupe'),
  2017,
  'El primer 718 Cayman con el 2.0 turbo de cuatro cilindros generó polémica entre puristas del flat-six. Sin embargo, los 300 HP, el PDK y la plataforma renovada hicieron del S un auto tremendamente capaz en pista.',
  'combustion', 300, 6, 'manual',
  8.5, 275, 58000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Porsche'),
  '718 Cayman',
  (SELECT body_type_id FROM body_types WHERE name = 'Coupe'),
  2021,
  'La versión GTS 4.0 del 718 Cayman recuperó el flat-six atmosférico de 4 litros y 400 HP que los puristas exigían. Sin turbo, sin filtros: el motor llega a 8.000 RPM con un sonido que ningún motor de cuatro cilindros puede imitar.',
  'combustion', 400, 6, 'manual',
  10.9, 293, 87000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Porsche'),
  '718 Boxster',
  (SELECT body_type_id FROM body_types WHERE name = 'Roadster'),
  2017,
  'El roadster 718 Boxster con motor central y el 2.0 turbo de cuatro cilindros. La versión S de 350 HP desequilibra a rivales mucho más caros. El Boxster S sigue siendo el mejor roadster del planeta en relación prestaciones/precio.',
  'combustion', 300, 6, 'manual',
  8.5, 275, 62000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Porsche'),
  '718 Boxster',
  (SELECT body_type_id FROM body_types WHERE name = 'Roadster'),
  2021,
  'El 718 Boxster GTS 4.0 marca el fin de una era: el último Boxster puramente atmosférico antes de la electrificación. El flat-six de 4 litros en posición central produce un sonido tan bueno como su velocidad en pista.',
  'combustion', 400, 6, 'manual',
  10.9, 293, 91000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Porsche'),
  'Cayenne',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2018,
  'La tercera generación PO536 del Cayenne estrena plataforma MSB compartida con el Panamera. El 3.0 V6 de 340 HP base es suficientemente capaz en cualquier situación, con un interior que da envidia a muchos sedanes de lujo.',
  'combustion', 340, 8, 'automatic',
  9.4, 263, 73000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Porsche'),
  'Cayenne',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2023,
  'El Cayenne Turbo E-Hybrid 2023 con el V8 4.0 biturbo más motor eléctrico totaliza 739 HP y puede recorrer 90 km en modo eléctrico. El SUV más rápido del mundo en Nürburgring en su categoría.',
  'combustion', 541, 8, 'automatic',
  11.9, 286, 136000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Porsche'),
  'Macan',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2019,
  'El Macan de primera generación con el 2.0 turbo de 252 HP es la puerta de entrada más vendida de Porsche. Plataforma del Audi Q5, pero con el ADN deportivo que solo Porsche sabe inyectar en un SUV familiar.',
  'combustion', 252, 8, 'automatic',
  9.2, 232, 59000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Porsche'),
  'Macan',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2022,
  'El Macan GTS facelift 95B con el 2.9 V6 biturbo de 380 HP es el balance ideal entre el Macan base y el Turbo. Sport Chrono de serie, suspensión PASM rebajada 10 mm y modos de conducción que van de turista a desbocado.',
  'combustion', 380, 8, 'automatic',
  9.5, 259, 83000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Porsche'),
  'Panamera',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2017,
  'La segunda generación 971 del Panamera rediseñó la controversial silueta de la primera. El 3.0 V6 de 330 HP base ya es suficientemente capaz, aunque el Turbo S E-Hybrid de 689 HP es el verdadero statement.',
  'combustion', 330, 8, 'automatic',
  9.8, 280, 98000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Porsche'),
  'Taycan',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2020,
  'El primer Porsche eléctrico de producción en serie llegó en 2019 con arquitectura de 800V para carga rápida a 270 kW. La versión Turbo S con Launch Control entrega 761 HP y hace 0-100 en 2.8 segundos. El futuro llegó.',
  'electric', 761, 8, 'automatic',
  0.0, 260, 185000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Porsche'),
  'Taycan',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2023,
  'La versión 4S del Taycan 2023 con autonomía ampliada hasta 576 km WLTP y el nuevo pack de batería Performance Plus de 93.4 kWh. Actualización OTA y sistema de infotainment totalmente renovado.',
  'electric', 476, 8, 'automatic',
  0.0, 250, 112000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Porsche'),
  '718 Cayman GT4',
  (SELECT body_type_id FROM body_types WHERE name = 'Coupe'),
  2020,
  'El Cayman GT4 con el flat-six 4.0 de 420 HP heredado del 911 GT3 es el auto de pista accesible definitivo. Alerón trasero fijo, aerodinámica activa y una caja manual de seis con doble masa volante de aluminio forjado.',
  'combustion', 420, 6, 'manual',
  10.9, 304, 99000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

-- ─────────────────────────────────────────────────────────
-- HONDA
-- ─────────────────────────────────────────────────────────

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Honda'),
  'Civic',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2016,
  'La décima generación FC1 del Civic marcó el regreso del turbo a Honda de producción. El 1.5 VTEC Turbo de 158 HP ofreció más potencia que el anterior V4 de 2.4L con menor consumo. Diseño polarizante que enamoró a muchos.',
  'combustion', 158, 6, 'automatic',
  6.8, 210, 21900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Honda'),
  'Civic',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2022,
  'La undécima generación FL1 volvió a un diseño más sobrio y elegante después del exceso geométrico de la décima. El 1.5 VTEC Turbo mantiene 158 HP pero con un carácter más maduro y un interior que parece de un segmento superior.',
  'combustion', 158, 8, 'automatic',
  6.4, 220, 24900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Honda'),
  'Accord',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2018,
  'La décima generación CV del Accord con el 1.5 VTEC Turbo de 192 HP o el 2.0 turbo del Type R de 252 HP. La versión Sport 2.0T es un sleeper familiar que manda al rincón a muchos sedanes deportivos del doble de precio.',
  'combustion', 192, 8, 'automatic',
  7.6, 215, 27000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Honda'),
  'CR-V',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2017,
  'La quinta generación RW del CR-V debutó con el 1.5 VTEC Turbo de 190 HP, la primera vez que el SUV usaba motor sobrealimentado. Fue el primer CR-V disponible en versión híbrida en algunos mercados.',
  'combustion', 190, 6, 'automatic',
  7.0, 200, 31000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Honda'),
  'HR-V',
  (SELECT body_type_id FROM body_types WHERE name = 'Crossover'),
  2022,
  'La tercera generación RV del HR-V llegó exclusivamente como e:HEV en muchos mercados, combinando el 1.5 con motor eléctrico para 131 HP totales. Sin pedal de embrague ni CVT ruidosa: la transmisión eléctrica directa es revolucionaria.',
  'hybrid', 131, 6, 'automatic',
  5.4, 173, 28900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Honda'),
  'Fit',
  (SELECT body_type_id FROM body_types WHERE name = 'Hatchback'),
  2015,
  'La tercera generación GK del Jazz/Fit con los famosos asientos Magic Seat que permiten configurar el interior de seis maneras distintas. El 1.5 i-VTEC de 130 HP es eficiente y confiable, ideal para la ciudad.',
  'combustion', 130, 6, 'automatic',
  5.6, 191, 16900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Honda'),
  'NSX',
  (SELECT body_type_id FROM body_types WHERE name = 'Coupe'),
  2017,
  'La segunda generación NC1 del NSX llegó en 2016 con el 3.5 V6 biturbo más tres motores eléctricos: 573 HP totales y AWD híbrido inteligente. La caja DCT de 9 marchas fue la primera de su tipo en producción.',
  'hybrid', 573, 6, 'automatic',
  11.0, 307, 157000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Honda'),
  'NSX',
  (SELECT body_type_id FROM body_types WHERE name = 'Coupe'),
  2022,
  'La edición Type S de 2022 fue la despedida del NSX NC1: 581 HP gracias al motor V6 sobrealimentado más potente y motores eléctricos recalibrados. Solo 350 unidades para todo el mundo; el fin de una era hybrid deportiva.',
  'hybrid', 581, 6, 'automatic',
  11.0, 307, 169000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Honda'),
  'S2000',
  (SELECT body_type_id FROM body_types WHERE name = 'Roadster'),
  2000,
  'El F20C del S2000 AP1 fue el motor de producción atmosférico de mayor rendimiento específico por litro de su época: 125 HP/L. Con 9.000 RPM de límite y caja de seis, el S2000 es el roadster de ensueño de los entendidos del JDM.',
  'combustion', 240, 2, 'manual',
  9.9, 240, NULL
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

-- ─────────────────────────────────────────────────────────
-- SUBARU
-- ─────────────────────────────────────────────────────────

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Subaru'),
  'Impreza',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2017,
  'La quinta generación GK/GT del Impreza sobre la nueva plataforma SGP. El boxer 2.0 atmosférico de 152 HP con Lineartronic CVT y AWD simétrico de serie: la base genética del WRX en versión civilizada y práctica.',
  'combustion', 152, 7, 'automatic',
  7.6, 194, 20900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Subaru'),
  'Legacy',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2020,
  'La séptima generación BW del Legacy sobre la plataforma SGP gana rigidez y pérdida de peso simultáneamente. El 2.5 boxer atmosférico de 182 HP con el EyeSight 4.0 de asistencia activa convierte a este sedán en referencia de seguridad.',
  'combustion', 182, 8, 'automatic',
  8.1, 215, 24900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Subaru'),
  'Legacy',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2023,
  'La versión XT 2023 del Legacy BW con el 2.4 turbo boxer de 260 HP y par de 376 Nm da vida al sedán familiar. El mayor torque entre los sedanes Subaru de producción en este mercado.',
  'combustion', 260, 8, 'automatic',
  8.4, 225, 32900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Subaru'),
  'Outback',
  (SELECT body_type_id FROM body_types WHERE name = 'Estate'),
  2020,
  'La sexta generación BT del Outback con la plataforma SGP eliminó el viejo motor flat-six pero mantuvo los 22 cm de despeje. El nuevo 2.5 boxer de 182 HP con CVT Lineartronic y EyeSight 4.0 redefine el concept de la rural alta.',
  'combustion', 182, 8, 'automatic',
  9.1, 210, 32900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Subaru'),
  'Forester',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2019,
  'La quinta generación SK del Forester sobre la nueva plataforma SGP perdió el motor turbo y la caja manual para tristeza de algunos. El 2.5 atmosférico de 182 HP con EyeSight de serie y la mayor visibilidad de su segmento compensan.',
  'combustion', 182, 7, 'automatic',
  8.1, 195, 28900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Subaru'),
  'Forester',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2023,
  'El facelift 2023 del Forester SK actualiza la pantalla central a 11.6 pulgadas y suma el EyeSight de cuarta generación con visión de 360 grados. Sin cambios mecánicos pero con más tecnología que justifica el precio mayor.',
  'combustion', 182, 7, 'automatic',
  8.1, 195, 30900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Subaru'),
  'BRZ',
  (SELECT body_type_id FROM body_types WHERE name = 'Coupe'),
  2022,
  'La segunda generación ZD8 del BRZ reemplaza el 2.0 por el 2.4 FA24 de 228 HP en colaboración con Toyota. Propulsión trasera, caja de 6 manual, peso inferior a 1.300 kg: el coupé asequible que demuestra que la diversión no tiene precio.',
  'combustion', 228, 6, 'manual',
  9.5, 226, 29000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Subaru'),
  'Crosstrek',
  (SELECT body_type_id FROM body_types WHERE name = 'Crossover'),
  2024,
  'La tercera generación GU del Crosstrek (XV en algunos mercados) sobre la plataforma SGP actualizada. El 2.5 boxer de 182 HP reemplaza al 2.0 anterior, sumando potencia y par para el uso off-road que sus compradores esperan.',
  'combustion', 182, 7, 'automatic',
  8.0, 198, 27900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Subaru'),
  'Ascent',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2019,
  'El primer SUV de tres filas de Subaru, lanzado para el mercado norteamericano en 2018. Con el 2.4 turbo boxer de 260 HP y capacidad para ocho pasajeros, llena el hueco entre el Forester y los grandes SUVs americanos.',
  'combustion', 260, 8, 'automatic',
  9.8, 209, 35000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

-- ─────────────────────────────────────────────────────────
-- NISSAN
-- ─────────────────────────────────────────────────────────

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Nissan'),
  'Sentra',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2015,
  'La generación B17 del Sentra con el 1.8 MR18DE atmosférico de 130 HP y transmisión Xtronic CVT. Básico pero fiable, el Sentra fue durante años el sedán compacto de referencia para el mercado latinoamericano de Nissan.',
  'combustion', 130, 6, 'automatic',
  7.8, 192, 16500.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Nissan'),
  'Sentra',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2021,
  'La octava generación B18 del Sentra con el nuevo 2.0 SR20DE de 149 HP marcó un salto cualitativo enorme. Diseño que recuerda al Altima, interior de primer nivel para el segmento y la primera suspensión independiente trasera del Sentra.',
  'combustion', 149, 8, 'automatic',
  6.6, 200, 20900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Nissan'),
  'Skyline GT-R',
  (SELECT body_type_id FROM body_types WHERE name = 'Coupe'),
  1999,
  'El R34 GT-R con el mítico RB26DETT 2.6 inline-six biturbo: 280 HP declarados (bastante más en la realidad). La electrónica ATTESA E-TS Pro con pantalla LCD en el tablero y el frente renovado lo convirtieron en ícono absoluto del JDM.',
  'combustion', 280, 2, 'manual',
  11.7, 250, NULL
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Nissan'),
  'Frontier',
  (SELECT body_type_id FROM body_types WHERE name = 'Pickup'),
  2017,
  'La generación D23 del Frontier/Navara con el 2.3 biturbo YD25DDTi de 190 HP fabricada en parte en la planta Santa Isabel de Córdoba. Alternativa seria a la Hilux con mecánicas Renault y chasis actualizado.',
  'combustion', 190, 7, 'automatic',
  7.0, 184, 31500.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Nissan'),
  'Frontier',
  (SELECT body_type_id FROM body_types WHERE name = 'Pickup'),
  2022,
  'El rediseño completo 2022 del Frontier para el mercado americano con el nuevo 3.8 V6 de 310 HP. Plataforma renovada, interior completamente nuevo y la versión PRO-4X con bloqueo de diferencial trasero electrónico.',
  'combustion', 310, 7, 'automatic',
  9.5, 185, 41000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Nissan'),
  'X-Trail',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2015,
  'La tercera generación T32 del X-Trail con el 2.0 MR20DD de 141 HP y opcional de tercera fila de asientos. La tracción All Mode 4x4 con modos auto, 2WD, 4WD lock la hace competente en suelo mixto.',
  'combustion', 141, 6, 'automatic',
  7.5, 190, 27000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Nissan'),
  'X-Trail',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2022,
  'La cuarta generación T33 del X-Trail llega con el sistema e-POWER: el 1.5 turbo de tres cilindros genera electricidad exclusivamente para el motor eléctrico que mueve las ruedas. El SUV más vendido de Nissan a nivel global.',
  'hybrid', 204, 6, 'automatic',
  5.9, 170, 35000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Nissan'),
  'Patrol',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2013,
  'La sexta generación Y62 del Patrol con el VK56VD V8 de 5.6L y 400 HP: el 4x4 grande que compite con el Land Cruiser en el mercado de Oriente Medio y América Latina. Ocho asientos y el lujo de un barco en tierra.',
  'combustion', 400, 8, 'automatic',
  14.4, 210, 72000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Nissan'),
  'Leaf',
  (SELECT body_type_id FROM body_types WHERE name = 'Hatchback'),
  2018,
  'La segunda generación ZE1 del Leaf con batería de 40 kWh y motor de 150 HP. Ganador del World Car of the Year 2011 en su primera generación, el Leaf normalizó la electromovilidad de masas a nivel global.',
  'electric', 150, 6, 'automatic',
  0.0, 144, 32000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Nissan'),
  'Leaf',
  (SELECT body_type_id FROM body_types WHERE name = 'Hatchback'),
  2021,
  'El Leaf e+ 2021 con la batería de 62 kWh y motor de 217 HP duplica la autonomía WLTP hasta 385 km. La actualización más significativa del eléctrico japonés, aunque sin carga rápida CCS que sus rivales ya ofrecen.',
  'electric', 217, 6, 'automatic',
  0.0, 157, 38000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Nissan'),
  'Kicks',
  (SELECT body_type_id FROM body_types WHERE name = 'Crossover'),
  2017,
  'El Kicks P15 fabricado en México reemplazó al Juke en muchos mercados latinoamericanos. Con el 1.6 HR16DE de 118 HP y solo tracción delantera, apuesta por el consumo y precio accesible sobre las prestaciones off-road.',
  'combustion', 118, 6, 'automatic',
  6.7, 175, 20500.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

-- ─────────────────────────────────────────────────────────
-- CHEVROLET
-- ─────────────────────────────────────────────────────────

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Chevrolet'),
  'Onix',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2019,
  'La segunda generación del Onix con el 1.0 Turbo Flex de tres cilindros y 116 HP. El auto más vendido de Argentina durante varios años consecutivos, fabricado en General Motors Alvear con motor actualizado y nuevo diseño.',
  'combustion', 116, 6, 'automatic',
  5.9, 191, 14900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Chevrolet'),
  'Onix',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2022,
  'El Onix 2022 incorpora mejoras de conectividad con pantalla de 8 pulgadas y MyLink de serie en versiones medias. La mecánica 1.0T sin cambios: el motor más vendido del país sigue siendo el mismo y lo seguirá siendo.',
  'combustion', 116, 6, 'automatic',
  5.9, 191, 17200.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Chevrolet'),
  'Cruze',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2017,
  'La segunda generación del Cruze con el 1.4T de 153 HP y la nueva plataforma D2 comparte 40% de componentes con el Astra europeo. Referente de equipamiento en el segmento compacto argentino antes del boom de los SUVs.',
  'combustion', 153, 6, 'automatic',
  6.6, 210, 21500.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Chevrolet'),
  'Tracker',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2020,
  'El nuevo Tracker basado en la plataforma del Onix con el 1.2T de 116 HP. Fabricado en la planta de Rosario desde 2021, se convirtió rápidamente en el SUV más vendido del mercado argentino.',
  'combustion', 116, 6, 'automatic',
  6.3, 185, 22500.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Chevrolet'),
  'Tracker',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2023,
  'La actualización 2023 del Tracker incorpora el 1.2T de 132 HP con mayor torque y la versión RS Turbo en tope de gama. Sigue siendo el SUV compacto de referencia en Argentina, ahora con mejor equipamiento de serie.',
  'combustion', 132, 6, 'automatic',
  6.3, 194, 26900.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Chevrolet'),
  'S10',
  (SELECT body_type_id FROM body_types WHERE name = 'Pickup'),
  2017,
  'La segunda generación de la S10 brasileña (Colorado en otros mercados) con el 2.8 Duramax turbodiesel de 200 HP. Pick-up mediana fabricada en São José dos Campos que en Argentina compite directamente con la Hilux y la Ranger.',
  'combustion', 200, 6, 'manual',
  8.7, 175, 31000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Chevrolet'),
  'S10',
  (SELECT body_type_id FROM body_types WHERE name = 'Pickup'),
  2021,
  'El facelift 2021 de la S10 brasileña renovó frente, interior y tecnología manteniendo el Duramax 2.8 de 200 HP. La versión High Country en automático es la opción ejecutiva de la pick-up mediana de GM en Sudamérica.',
  'combustion', 200, 6, 'automatic',
  8.7, 175, 36000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Chevrolet'),
  'Corvette',
  (SELECT body_type_id FROM body_types WHERE name = 'Coupe'),
  2020,
  'La octava generación C8 del Corvette democratizó el motor central por primera vez en la historia del modelo. El 6.2 LT2 de 495 HP hace 0-100 en 2.9 segundos a un precio que avergüenza a los italianos mid-engine.',
  'combustion', 495, 8, 'automatic',
  12.1, 312, 60000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Chevrolet'),
  'Corvette',
  (SELECT body_type_id FROM body_types WHERE name = 'Coupe'),
  2023,
  'El C8 Z06 2023 con el motor LT6 V8 de 5.5L plano de 670 HP y 8.600 RPM: el primer Corvette con motor de alta revolución naturalmente aspirado en producción. El Flat-Plane V8 es tecnología de Fórmula 1 para la calle.',
  'combustion', 670, 8, 'automatic',
  13.5, 312, 110000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Chevrolet'),
  'Silverado',
  (SELECT body_type_id FROM body_types WHERE name = 'Pickup'),
  2019,
  'La cuarta generación T1XX del Silverado con el 5.3 EcoTec3 V8 de 355 HP y tecnología Dynamic Fuel Management que desactiva hasta 6 cilindros. La pick-up grande americana por excelencia en su versión más vendida.',
  'combustion', 355, 6, 'automatic',
  11.8, 180, 42000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Chevrolet'),
  'Equinox',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2018,
  'La tercera generación del Equinox con el 1.5T de 170 HP inauguró los motores pequeños en el SUV mediano de GM. Fabricado en Canadá y México, combina espacio familiar con un consumo razonable para el segmento.',
  'combustion', 170, 7, 'automatic',
  7.6, 200, 28000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

-- ─────────────────────────────────────────────────────────
-- AUDI
-- ─────────────────────────────────────────────────────────

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Audi'),
  'A3',
  (SELECT body_type_id FROM body_types WHERE name = 'Hatchback'),
  2013,
  'La tercera generación 8V del A3 Sportback sobre la plataforma MQB con el 1.4 TFSI de 150 HP y la caja S-tronic de 7 marchas. El compacto premium que establece el estándar de calidad interior en su segmento.',
  'combustion', 150, 6, 'automatic',
  5.4, 230, 29000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Audi'),
  'A3',
  (SELECT body_type_id FROM body_types WHERE name = 'Hatchback'),
  2021,
  'La cuarta generación 8Y del A3 Sportback con la pantalla MMI de 10.1 pulgadas integrada horizontalmente. El 35 TFSI de 150 HP mantiene mecánica pero el salto tecnológico interior es sustancial respecto al 8V.',
  'combustion', 150, 7, 'automatic',
  5.3, 230, 34000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Audi'),
  'A4',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2016,
  'La quinta generación B9 del A4 sobre la plataforma MLB evo ganó 120 kg respecto al B8. El 2.0 TFSI de 190 HP con quattro opcional y la Digital Cockpit de 12.3 pulgadas elevaron el estándar del segmento ejecutivo.',
  'combustion', 190, 8, 'automatic',
  5.9, 250, 42000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Audi'),
  'A4',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2020,
  'El facelift B9.5 del A4 2020 con el nuevo 2.0 TFSI de 204 HP con sistema mild-hybrid 12V integrado. La Digital Cockpit Pro de 12.3 y el nuevo MMI touch con control de voz mejorado modernizan la experiencia interior.',
  'combustion', 204, 8, 'automatic',
  6.0, 250, 47000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Audi'),
  'Q5',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2017,
  'La segunda generación FY del Q5 sobre la plataforma MLB evo fue por años el modelo más vendido de Audi a nivel global. El 2.0 TFSI quattro de 252 HP combina tracción integral y un consumo que desafía al motor para su potencia.',
  'combustion', 252, 8, 'automatic',
  7.5, 237, 52000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Audi'),
  'Q5',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2021,
  'El Q5 55 TFSI e quattro PHEV con el 2.0 TFSI más motor eléctrico totaliza 299 HP y hasta 43 km de autonomía eléctrica WLTP. La opción perfecta para quien maneja ciudad en modo eléctrico y autovía en modo híbrido.',
  'hybrid', 299, 8, 'automatic',
  2.0, 237, 62000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Audi'),
  'Q7',
  (SELECT body_type_id FROM body_types WHERE name = 'SUV'),
  2016,
  'La segunda generación 4M del Q7 perdió 325 kg respecto a su predecesor gracias al aluminio y el acero de ultra-alta resistencia. El 3.0 TFSI V6 de 252 HP en la base ya es más que suficiente para mover tres toneladas con dignidad.',
  'combustion', 252, 8, 'automatic',
  9.0, 243, 72000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Audi'),
  'TT',
  (SELECT body_type_id FROM body_types WHERE name = 'Coupe'),
  2015,
  'La tercera y última generación 8S del TT con el Virtual Cockpit de 12.3 pulgadas que cambió la instrumentación para siempre. El 2.0 TFSI de 230 HP en la versión base ya es divertido; el TTS de 310 HP más aún.',
  'combustion', 230, 6, 'automatic',
  6.8, 250, 42000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Audi'),
  'TT',
  (SELECT body_type_id FROM body_types WHERE name = 'Coupe'),
  2019,
  'El TTS 8S de 306 HP en su versión final antes de la descontinuación en 2023. El quattro de serie, el diferencial electrónico VAQ y la dinámica afilada hacen del TTS uno de los coupés más completos que desaparecieron sin razón aparente.',
  'combustion', 306, 6, 'automatic',
  7.0, 250, 56000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Audi'),
  'R8',
  (SELECT body_type_id FROM body_types WHERE name = 'Coupe'),
  2016,
  'La segunda generación 4S del R8 V10 comparte plataforma con el Lamborghini Huracán. El V10 atmosférico de 5.2L a 8.700 RPM produce 540 HP en versión base y 610 en Performance: la experiencia de un superauto puro antes de los turbo.',
  'combustion', 540, 6, 'automatic',
  13.1, 324, 162000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Audi'),
  'R8',
  (SELECT body_type_id FROM body_types WHERE name = 'Coupe'),
  2020,
  'El facelift R8 V10 Performance de 620 HP es el último R8 antes de su despedida en 2023. Sin turbo, sin hibridación: el V10 puro que chilla hasta 8.700 RPM es una especie en extinción que ningún número sobre papel puede transmitir.',
  'combustion', 620, 6, 'automatic',
  13.1, 331, 195000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

INSERT INTO cars
  (brand_id, model, body_type_id, year, description,
   fuel_type, horsepower, airbag_count, transmission,
   fuel_consumption, max_speed_kmh, price_usd)
SELECT
  (SELECT brand_id    FROM brands     WHERE name = 'Audi'),
  'e-tron GT',
  (SELECT body_type_id FROM body_types WHERE name = 'Sedan'),
  2021,
  'El gran turismo eléctrico que comparte la plataforma J1 con el Porsche Taycan. Lanzado en 2021, la versión RS con 637 HP (646 con boost) y arquitectura de 800V carga de 5 a 80% en menos de 23 minutos.',
  'electric', 637, 8, 'automatic',
  0.0, 250, 110000.00
ON CONFLICT ON CONSTRAINT uq_cars_brand_model_body_type_year DO NOTHING;

