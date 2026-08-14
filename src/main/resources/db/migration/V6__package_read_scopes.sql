-- Divide la lectura de paquetes en dos scopes: ver todos vs. ver los propios.
UPDATE permission SET name = 'package:read:all' WHERE name = 'package:read';

INSERT INTO permission (name) VALUES ('package:read:own')
ON CONFLICT (name) DO NOTHING;

-- CUSTOMER (role_id 5) lee sus propios paquetes.
INSERT INTO role_permission (role_id, permission_id)
SELECT 5, id FROM permission WHERE name = 'package:read:own'
ON CONFLICT DO NOTHING;
