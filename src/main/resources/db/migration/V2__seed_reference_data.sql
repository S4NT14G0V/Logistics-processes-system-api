INSERT INTO role (id, name, description) VALUES
    (1, 'ADMIN', 'Administrator'),
    (2, 'LOGISTICS', 'Logistics operator'),
    (3, 'WAREHOUSE', 'Warehouse operator'),
    (4, 'SUPERVISOR', 'Supervisor'),
    (5, 'CUSTOMER', 'Customer')
ON CONFLICT (id) DO NOTHING;

INSERT INTO package_status (id, name, description) VALUES
    (1, 'Created', 'Package created'),
    (2, 'In Transit', 'Package in transit'),
    (3, 'Delivered', 'Package delivered'),
    (4, 'Cancelled', 'Package cancelled')
ON CONFLICT (id) DO NOTHING;

INSERT INTO alert_type (id, name, description) VALUES
    (1, 'Delayed', 'Package delayed'),
    (2, 'Lost', 'Package lost'),
    (3, 'Damaged', 'Package damaged')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('role', 'id'), (SELECT max(id) FROM role));
SELECT setval(pg_get_serial_sequence('package_status', 'id'), (SELECT max(id) FROM package_status));
SELECT setval(pg_get_serial_sequence('alert_type', 'id'), (SELECT max(id) FROM alert_type));
