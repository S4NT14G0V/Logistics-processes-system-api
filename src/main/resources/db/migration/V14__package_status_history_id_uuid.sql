-- Convierte el id de package_status_history de bigint a uuid.
ALTER TABLE package_status_history ADD COLUMN id_uuid uuid;
UPDATE package_status_history SET id_uuid = gen_random_uuid();
ALTER TABLE package_status_history ALTER COLUMN id_uuid SET NOT NULL;

ALTER TABLE package_status_history DROP CONSTRAINT package_status_history_pkey;
ALTER TABLE package_status_history DROP COLUMN id;
ALTER TABLE package_status_history RENAME COLUMN id_uuid TO id;
ALTER TABLE package_status_history ADD PRIMARY KEY (id);
