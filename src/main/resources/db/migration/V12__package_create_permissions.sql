-- Renombra package:create a su forma :all para consistencia con update/cancel.
UPDATE permission SET name = 'package:create:all' WHERE name = 'package:create';

-- Permiso :own para que el CUSTOMER proponga sus propios envíos.
INSERT INTO permission (name) VALUES ('package:create:own')
ON CONFLICT (name) DO NOTHING;

-- CUSTOMER (role_id 5) puede proponer envíos propios.
INSERT INTO role_permission (role_id, permission_id)
SELECT 5, id FROM permission WHERE name = 'package:create:own'
ON CONFLICT DO NOTHING;
