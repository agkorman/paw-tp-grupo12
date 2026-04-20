-- Populate reviews for the cars seeded in schema.sql.
-- This file is rerunnable: each review is skipped if a review with the same
-- car, reviewer_email, and title already exists.

WITH review_seed (
    brand_name,
    model,
    body_type,
    reviewer_email,
    rating,
    title,
    body,
    ownership_status,
    model_year,
    mileage_km,
    would_recommend,
    created_at
) AS (
    VALUES
        ('Toyota', 'GR Supra', 'Coupe', 'nico.supra@example.com', 4.5, 'Anda fuerte de verdad', 'En ruta empuja una barbaridad y la caja acompaña siempre. Para ciudad es mas duro y la visibilidad atras no ayuda.', 'Propietario actual', 2021, 28000, TRUE, '2024-03-12 10:15:00+00'),
        ('Toyota', 'GR Supra', 'Coupe', 'vane.coupe@example.com', 4.0, 'Divertido, poco practico', 'Te saca una sonrisa en cada salida, pero para usarlo todos los dias pide paciencia. Espacio justo y suspension firme.', 'Ex propietario', 2020, 43000, TRUE, '2024-08-02 18:40:00+00'),
        ('Toyota', 'GR Supra', 'Coupe', 'mati.track@example.com', 3.5, 'Gran coupe para escapadas', 'Para fin de semana va barbaro. En calle rota rebota un poco, pero en curvas se siente muy solido.', 'Propietario actual', 2022, 15000, TRUE, '2025-01-19 13:05:00+00'),

        ('Ford', 'Mustang', 'Coupe', 'fer.v8@example.com', 4.5, 'Mucho motor, mucho carisma', 'El V8 suena tremendo y en ruta va re comodo. En ciudad se nota largo y gasta sin culpa.', 'Propietario actual', 2020, 36000, TRUE, '2024-02-18 09:20:00+00'),
        ('Ford', 'Mustang', 'Coupe', 'agus.blueoval@example.com', 3.5, 'Mas comodo de lo esperado', 'Pensaba que iba a ser torpe y no. Dobla mejor de lo que parece, aunque el interior podria ser mas especial.', 'Ex propietario', 2019, 58000, TRUE, '2024-11-07 21:10:00+00'),
        ('Ford', 'Mustang', 'Coupe', 'sol.fastback@example.com', 4.0, 'Hermoso, pero no es chico', 'Para viajar es un lujo. Para estacionar en ciudad y moverse en lugares apretados, no tanto.', 'Propietario actual', 2021, 22000, TRUE, '2025-02-26 16:30:00+00'),

        ('Mazda', 'MX-5 Miata', 'Roadster', 'clara.miata@example.com', 5.0, 'Chiquito y muy divertido', 'No necesita tanta potencia para engancharte. Dobla barbaro y con techo abajo es una fiesta.', 'Propietario actual', 2023, 9000, TRUE, '2024-01-30 11:00:00+00'),
        ('Mazda', 'MX-5 Miata', 'Roadster', 'fran.topdown@example.com', 4.5, 'Auto para disfrutar', 'Caja manual impecable y peso bajisimo. En autopista con viento se mueve mas de lo que me gustaria.', 'Ex propietario', 2021, 27000, TRUE, '2024-09-14 17:25:00+00'),
        ('Mazda', 'MX-5 Miata', 'Roadster', 'pau.weekend@example.com', 4.0, 'Siempre dan ganas de manejarlo', 'Hasta para hacer un mandado sirve de excusa para salir. El punto flojo es el espacio, que es minimo.', 'Propietario actual', 2022, 14000, TRUE, '2025-03-03 14:45:00+00'),

        ('BMW', 'M3', 'Sedan', 'rodrigo.m3@example.com', 4.5, 'Sedan que hace todo bien', 'Anda fortisimo y a la vez sirve para todos los dias. Lo que duele de verdad es mantenerlo.', 'Propietario actual', 2022, 18000, TRUE, '2024-04-21 12:10:00+00'),
        ('BMW', 'M3', 'Sedan', 'laura.s58@example.com', 4.0, 'Rapido y muy preciso', 'El chasis es tremendo y transmite mucha seguridad. En calles feas se siente firme y las cubiertas salen caras.', 'Ex propietario', 2021, 34000, TRUE, '2024-10-28 19:50:00+00'),
        ('BMW', 'M3', 'Sedan', 'diego.dailym@example.com', 3.5, 'Brilla mas en ruta', 'La ciudad no es su mejor ambiente, pero cuando la calle se abre aparece todo lo bueno. Empuje y frenos de sobra.', 'Propietario actual', 2023, 12000, TRUE, '2025-02-10 08:55:00+00'),

        ('Porsche', '911', 'Coupe', 'martin.992@example.com', 5.0, 'Muy fino en todo', 'Se siente bien resuelto por todos lados. Va rapido sin asustar y sorprende lo facil que es usarlo.', 'Propietario actual', 2022, 11000, TRUE, '2024-05-06 15:35:00+00'),
        ('Porsche', '911', 'Coupe', 'ana.flatsix@example.com', 4.5, 'Rapido y usable', 'Tiene precision de sobra y una posicion de manejo excelente. Cada service sale caro, pero el auto responde.', 'Ex propietario', 2021, 26000, TRUE, '2024-12-01 20:05:00+00'),
        ('Porsche', '911', 'Coupe', 'guille.carrera@example.com', 4.0, 'Dificil bajarse', 'Todo lo hace bien: motor, frenos, direccion y calidad. El unico gran problema es lo que vale tenerlo.', 'Propietario actual', 2020, 31000, TRUE, '2025-03-18 10:40:00+00'),

        ('Honda', 'Civic Type R', 'Hatchback', 'eze.typeR@example.com', 4.5, 'Manual de los que ya no quedan', 'La caja es una delicia y el tren delantero se la banca muy bien. Suspension firme para calles rotas.', 'Propietario actual', 2023, 8000, TRUE, '2024-06-15 09:45:00+00'),
        ('Honda', 'Civic Type R', 'Hatchback', 'juli.trackday@example.com', 4.0, 'Hot hatch serio', 'Es rapidisimo y avisa bien lo que hace el auto. Si no te gusta llamar la atencion, no es el ideal.', 'Ex propietario', 2022, 19000, TRUE, '2024-11-22 18:15:00+00'),
        ('Honda', 'Civic Type R', 'Hatchback', 'meli.hothatch@example.com', 3.5, 'Mecanica impecable, look discutible', 'Anda barbaro y frena muy bien. Lo que menos me cierra es lo exagerado del diseno.', 'Propietario actual', 2023, 6000, TRUE, '2025-02-14 13:20:00+00'),

        ('Subaru', 'WRX STI', 'Sedan', 'seba.awd@example.com', 4.5, 'En lluvia se luce', 'Donde otros patinan, este sale disparado. Gasta bastante y el interior quedo viejo, pero tiene mucho carisma.', 'Ex propietario', 2018, 52000, TRUE, '2024-03-28 16:00:00+00'),
        ('Subaru', 'WRX STI', 'Sedan', 'lucas.boxer@example.com', 4.0, 'Se la banca en calle fea', 'En piso roto y curvas rapidas transmite una confianza enorme. Para el dia a dia es bastante duro.', 'Propietario actual', 2017, 76000, TRUE, '2024-09-30 12:35:00+00'),
        ('Subaru', 'WRX STI', 'Sedan', 'caro.rallyblue@example.com', 3.0, 'Solo para el que lo quiere', 'Tiene personalidad de sobra, pero tambien varios caprichos. Si no te bancas el consumo y el ruido, te cansa.', 'Propietario actual', 2016, 98000, FALSE, '2025-01-27 19:05:00+00'),

        ('Nissan', 'GT-R', 'Coupe', 'fede.gtr@example.com', 4.5, 'Acelera como loco', 'Cada vez que lo pisas te pega al asiento. En ciudad se siente pesado, pero en ruta vuela.', 'Propietario actual', 2018, 24000, TRUE, '2024-04-09 11:50:00+00'),
        ('Nissan', 'GT-R', 'Coupe', 'vero.r35@example.com', 4.0, 'Bestia con costos de bestia', 'Prestaciones espectaculares y mucho grip. Mantenerlo bien no es barato y el interior no deslumbra.', 'Ex propietario', 2017, 41000, TRUE, '2024-10-12 20:20:00+00'),
        ('Nissan', 'GT-R', 'Coupe', 'tomi.launch@example.com', 3.5, 'Mejor en la ruta', 'En ciudad no lo disfrute tanto, pero cuando tenes espacio cambia todo. El conjunto mecanico impresiona.', 'Propietario actual', 2016, 48000, TRUE, '2025-03-07 17:40:00+00'),

        ('Chevrolet', 'Camaro', 'Coupe', 'nacho.camaro@example.com', 4.0, 'Suena y anda como esperas', 'Tiene presencia y el V8 cumple con creces. La visibilidad es floja, pero el chasis esta mejor de lo que parece.', 'Propietario actual', 2019, 33000, TRUE, '2024-02-07 08:30:00+00'),
        ('Chevrolet', 'Camaro', 'Coupe', 'mora.ss@example.com', 3.5, 'Fachero, no tan practico', 'En ciudad cuesta por el tamano y los puntos ciegos. Igual cada vez que lo arrancas se te pasa un poco.', 'Ex propietario', 2018, 55000, TRUE, '2024-08-19 14:10:00+00'),
        ('Chevrolet', 'Camaro', 'Coupe', 'bruno.zl1@example.com', 4.5, 'Mas entretenido que racional', 'No es para buscar logica, es para disfrutarlo. En ruta va muy bien y el motor empuja siempre.', 'Propietario actual', 2020, 29000, TRUE, '2025-02-22 22:00:00+00'),

        ('Audi', 'RS6 Avant', 'Estate', 'sofi.rs6@example.com', 5.0, 'Hace todo', 'Lleva gente, valijas y encima acelera una locura. Para viajar es tremendo y adentro se siente premium.', 'Propietario actual', 2022, 17000, TRUE, '2024-05-25 10:25:00+00'),
        ('Audi', 'RS6 Avant', 'Estate', 'pablo.v8tt@example.com', 4.5, 'Rutero de altisimo nivel', 'En autopista va planchado y siempre le sobra motor. En ciudad se nota grande, pero sigue siendo comodisimo.', 'Ex propietario', 2021, 39000, TRUE, '2024-12-17 18:55:00+00'),
        ('Audi', 'RS6 Avant', 'Estate', 'ines.longroof@example.com', 4.0, 'Un misil familiar', 'No hay muchos autos que mezclen espacio, calidad y prestaciones asi. Lo incomodo es moverlo en lugares chicos.', 'Propietario actual', 2023, 12000, TRUE, '2025-03-25 12:00:00+00')
)
INSERT INTO reviews (
    reviewer_email,
    car_id,
    rating,
    title,
    body,
    ownership_status,
    model_year,
    mileage_km,
    would_recommend,
    created_at,
    updated_at
)
SELECT
    rs.reviewer_email,
    c.car_id,
    rs.rating,
    rs.title,
    rs.body,
    rs.ownership_status,
    rs.model_year,
    rs.mileage_km,
    rs.would_recommend,
    rs.created_at::timestamptz,
    rs.created_at::timestamptz
FROM review_seed rs
JOIN brands b
    ON b.name = rs.brand_name
JOIN body_types bt
    ON bt.name = rs.body_type
JOIN cars c
    ON c.brand_id = b.brand_id
   AND c.body_type_id = bt.body_type_id
   AND c.model = rs.model
WHERE NOT EXISTS (
    SELECT 1
    FROM reviews r
    WHERE r.car_id = c.car_id
      AND r.reviewer_email = rs.reviewer_email
);
