-- Estado PROPOSED: propuesta de envío pendiente de aprobación.
INSERT INTO package_status (id, code, name, description) VALUES
    (5, 'PROPOSED', 'Proposed', 'Package proposed, awaiting approval')
ON CONFLICT (id) DO NOTHING;

-- Columnas físicas y de precio en package.
ALTER TABLE package ADD COLUMN IF NOT EXISTS weight_kg double precision;
ALTER TABLE package ADD COLUMN IF NOT EXISTS length_cm double precision;
ALTER TABLE package ADD COLUMN IF NOT EXISTS width_cm double precision;
ALTER TABLE package ADD COLUMN IF NOT EXISTS height_cm double precision;
ALTER TABLE package ADD COLUMN IF NOT EXISTS distance_km double precision;
ALTER TABLE package ADD COLUMN IF NOT EXISTS declared_value double precision;
ALTER TABLE package ADD COLUMN IF NOT EXISTS price double precision;

SELECT setval(pg_get_serial_sequence('package_status', 'id'), (SELECT max(id) FROM package_status));
