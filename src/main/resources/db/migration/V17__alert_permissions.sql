-- Alertas: lectura (global/propia) y creación (notificaciones del staff).
INSERT INTO permission (name) VALUES ('alert:read:all')
ON CONFLICT (name) DO NOTHING;

INSERT INTO permission (name) VALUES ('alert:read:own')
ON CONFLICT (name) DO NOTHING;

INSERT INTO permission (name) VALUES ('alert:create:all')
ON CONFLICT (name) DO NOTHING;

-- ADMIN (role_id 1): lee y crea alertas.
INSERT INTO role_permission (role_id, permission_id)
SELECT 1, id FROM permission
WHERE name IN ('alert:read:all', 'alert:create:all')
ON CONFLICT DO NOTHING;

-- LOGISTICS (2) y SUPERVISOR (4): leen y crean alertas.
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name IN ('LOGISTICS', 'SUPERVISOR')
  AND p.name IN ('alert:read:all', 'alert:create:all')
ON CONFLICT DO NOTHING;

-- CUSTOMER (role_id 5): lee sus propias alertas.
INSERT INTO role_permission (role_id, permission_id)
SELECT 5, id FROM permission WHERE name = 'alert:read:own'
ON CONFLICT DO NOTHING;
