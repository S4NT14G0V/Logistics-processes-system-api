-- Renombra los permisos de actualización y borrado a su forma :all.
UPDATE permission SET name = 'package:update:all' WHERE name = 'package:update';
UPDATE permission SET name = 'package:cancel:all' WHERE name = 'package:delete';

-- Permisos :own para que el dueño gestione únicamente sus propios paquetes.
INSERT INTO permission (name) VALUES ('package:update:own')
ON CONFLICT (name) DO NOTHING;

INSERT INTO permission (name) VALUES ('package:cancel:own')
ON CONFLICT (name) DO NOTHING;

-- CUSTOMER (role_id 5) puede actualizar y cancelar sus propios paquetes.
INSERT INTO role_permission (role_id, permission_id)
SELECT 5, id FROM permission WHERE name IN ('package:update:own', 'package:cancel:own')
ON CONFLICT DO NOTHING;

-- El personal de operaciones puede cancelar cualquier paquete.
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name IN ('LOGISTICS', 'SUPERVISOR') AND p.name = 'package:cancel:all'
ON CONFLICT DO NOTHING;
