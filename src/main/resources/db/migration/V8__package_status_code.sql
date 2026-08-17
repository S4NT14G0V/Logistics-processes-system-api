ALTER TABLE package_status ADD COLUMN IF NOT EXISTS code varchar(255);

UPDATE package_status SET code = 'CREATED'     WHERE name = 'Created';
UPDATE package_status SET code = 'IN_TRANSIT'  WHERE name = 'In Transit';
UPDATE package_status SET code = 'DELIVERED'   WHERE name = 'Delivered';
UPDATE package_status SET code = 'CANCELLED'   WHERE name = 'Cancelled';

UPDATE package_status SET code = upper(replace(name, ' ', '_')) WHERE code IS NULL;

ALTER TABLE package_status ALTER COLUMN code SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_package_status_code ON package_status (code);
