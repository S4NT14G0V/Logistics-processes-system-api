-- Permite marcar usuarios creados por un admin con contraseña temporal.
ALTER TABLE users ADD COLUMN change_password boolean NOT NULL DEFAULT false;
