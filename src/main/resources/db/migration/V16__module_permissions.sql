-- User: divide los permisos existentes en :all y añade :own.
UPDATE permission SET name = 'user:create:all' WHERE name = 'user:create';
UPDATE permission SET name = 'user:read:all'   WHERE name = 'user:read';
UPDATE permission SET name = 'user:update:all' WHERE name = 'user:update';
UPDATE permission SET name = 'user:delete:all' WHERE name = 'user:delete';

INSERT INTO permission (name) VALUES ('user:read:own')
ON CONFLICT (name) DO NOTHING;

INSERT INTO permission (name) VALUES ('user:update:own')
ON CONFLICT (name) DO NOTHING;

-- Catálogo, ubicaciones e inventario.
INSERT INTO permission (name) VALUES ('catalog:read')
ON CONFLICT (name) DO NOTHING;

INSERT INTO permission (name) VALUES ('location:read:all')
ON CONFLICT (name) DO NOTHING;

INSERT INTO permission (name) VALUES ('location:read:own')
ON CONFLICT (name) DO NOTHING;

INSERT INTO permission (name) VALUES ('location:create:all')
ON CONFLICT (name) DO NOTHING;

INSERT INTO permission (name) VALUES ('location:update:all')
ON CONFLICT (name) DO NOTHING;

INSERT INTO permission (name) VALUES ('location:delete:all')
ON CONFLICT (name) DO NOTHING;

INSERT INTO permission (name) VALUES ('inventory:read')
ON CONFLICT (name) DO NOTHING;

-- ADMIN (role_id 1): todos los permisos excepto los scoped :own (redundantes con :all).
INSERT INTO role_permission (role_id, permission_id)
SELECT 1, id FROM permission WHERE name NOT LIKE '%:own'
ON CONFLICT DO NOTHING;

-- LOGISTICS (role_id 2): opera paquetes y consulta inventario/catálogo.
INSERT INTO role_permission (role_id, permission_id)
SELECT 2, id FROM permission
WHERE name IN ('catalog:read', 'location:read:all', 'location:create:all',
               'location:update:all', 'location:delete:all', 'inventory:read')
ON CONFLICT DO NOTHING;

-- WAREHOUSE (role_id 3): reporta ubicaciones en bodega.
INSERT INTO role_permission (role_id, permission_id)
SELECT 3, id FROM permission
WHERE name IN ('catalog:read', 'location:read:all', 'location:create:all')
ON CONFLICT DO NOTHING;

-- SUPERVISOR (role_id 4): supervisa e inventario.
INSERT INTO role_permission (role_id, permission_id)
SELECT 4, id FROM permission
WHERE name IN ('catalog:read', 'location:read:all', 'inventory:read')
ON CONFLICT DO NOTHING;

-- CUSTOMER (role_id 5): sus propios datos, contraseña y ubicaciones de sus paquetes.
INSERT INTO role_permission (role_id, permission_id)
SELECT 5, id FROM permission
WHERE name IN ('user:read:own', 'user:update:own', 'location:read:own')
ON CONFLICT DO NOTHING;
