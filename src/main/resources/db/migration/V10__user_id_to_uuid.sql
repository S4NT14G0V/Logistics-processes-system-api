-- Convierte el id de usuario de bigint a uuid.
-- El id bigint anterior solo se conserva de forma temporal para reenlazar las FKs.

-- 1. Nueva columna uuid en users + backfill.
ALTER TABLE users ADD COLUMN id_uuid uuid;
UPDATE users SET id_uuid = gen_random_uuid();
ALTER TABLE users ALTER COLUMN id_uuid SET NOT NULL;

-- 2. Columna uuid en cada tabla hija + backfill enlazando por el id bigint antiguo.
ALTER TABLE package ADD COLUMN owner_user_uuid uuid;
UPDATE package p SET owner_user_uuid = u.id_uuid FROM users u WHERE u.id = p.owner_user_id;

ALTER TABLE alert ADD COLUMN user_uuid uuid;
UPDATE alert a SET user_uuid = u.id_uuid FROM users u WHERE u.id = a.user_id;

ALTER TABLE location ADD COLUMN handler_user_uuid uuid;
UPDATE location l SET handler_user_uuid = u.id_uuid FROM users u WHERE u.id = l.handler_user_id;

ALTER TABLE refresh_token ADD COLUMN user_uuid uuid;
UPDATE refresh_token rt SET user_uuid = u.id_uuid FROM users u WHERE u.id = rt.user_id;

ALTER TABLE package_status_history ADD COLUMN changed_by_user_uuid uuid;
UPDATE package_status_history h SET changed_by_user_uuid = u.id_uuid FROM users u WHERE u.id = h.changed_by_user_id;

-- 2.1. Preserva la restricción NOT NULL de las FKs originales.
ALTER TABLE package ALTER COLUMN owner_user_uuid SET NOT NULL;
ALTER TABLE alert ALTER COLUMN user_uuid SET NOT NULL;
ALTER TABLE location ALTER COLUMN handler_user_uuid SET NOT NULL;
ALTER TABLE refresh_token ALTER COLUMN user_uuid SET NOT NULL;

-- 3. Dropea dinámicamente todas las FKs que referencian users (por nombre real de BD).
DO $$
DECLARE
    r record;
BEGIN
    FOR r IN
        SELECT conrelid::regclass::text AS tbl, conname
        FROM pg_constraint
        WHERE contype = 'f' AND confrelid = 'users'::regclass
    LOOP
        EXECUTE format('ALTER TABLE %s DROP CONSTRAINT %I', r.tbl, r.conname);
    END LOOP;
END $$;

-- 4. Elimina columnas bigint antiguas y renombra las nuevas uuid.
ALTER TABLE package DROP COLUMN owner_user_id;
ALTER TABLE package RENAME COLUMN owner_user_uuid TO owner_user_id;

ALTER TABLE alert DROP COLUMN user_id;
ALTER TABLE alert RENAME COLUMN user_uuid TO user_id;

ALTER TABLE location DROP COLUMN handler_user_id;
ALTER TABLE location RENAME COLUMN handler_user_uuid TO handler_user_id;

ALTER TABLE refresh_token DROP COLUMN user_id;
ALTER TABLE refresh_token RENAME COLUMN user_uuid TO user_id;

ALTER TABLE package_status_history DROP COLUMN changed_by_user_id;
ALTER TABLE package_status_history RENAME COLUMN changed_by_user_uuid TO changed_by_user_id;

-- 5. Intercambia la PK de users: bigint -> uuid.
ALTER TABLE users DROP CONSTRAINT users_pkey;
ALTER TABLE users DROP COLUMN id;
ALTER TABLE users RENAME COLUMN id_uuid TO id;
ALTER TABLE users ADD PRIMARY KEY (id);

-- 6. Recrea las FKs apuntando a users(id) uuid.
ALTER TABLE package
    ADD CONSTRAINT package_owner_user_id_fkey FOREIGN KEY (owner_user_id) REFERENCES users (id);
ALTER TABLE alert
    ADD CONSTRAINT alert_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id);
ALTER TABLE location
    ADD CONSTRAINT location_handler_user_id_fkey FOREIGN KEY (handler_user_id) REFERENCES users (id);
ALTER TABLE refresh_token
    ADD CONSTRAINT refresh_token_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id);
ALTER TABLE package_status_history
    ADD CONSTRAINT package_status_history_changed_by_user_id_fkey FOREIGN KEY (changed_by_user_id) REFERENCES users (id);
