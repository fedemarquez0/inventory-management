-- ===========================================
-- STORES
-- ===========================================
INSERT INTO stores (id, name, is_active, created_at, updated_at)
VALUES
    (1, 'Shopping Dinosaurio Mall', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Centro Maipu 712', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'Nuevo Centro Shopping', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ===========================================
-- USERS
-- ===========================================
-- Password for all users: "12345" (BCrypt hashed)
INSERT INTO users (id, username, password_hash, role, is_active, created_at, updated_at)
VALUES
    (1, 'admin', '$2a$10$64K/otiwP59MWSCrAWD8XezUo5l.v5k0X9zaV3S8NqKKbabW72WEa', 'ADMIN', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'user_dinosaurio', '$2a$10$64K/otiwP59MWSCrAWD8XezUo5l.v5k0X9zaV3S8NqKKbabW72WEa', 'STORE_USER', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'user_maipu', '$2a$10$64K/otiwP59MWSCrAWD8XezUo5l.v5k0X9zaV3S8NqKKbabW72WEa', 'STORE_USER', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 'user_nuevo_centro', '$2a$10$64K/otiwP59MWSCrAWD8XezUo5l.v5k0X9zaV3S8NqKKbabW72WEa', 'STORE_USER', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ===========================================
-- USER STORE PERMISSIONS
-- ===========================================
-- Usuarios de tienda tienen acceso solo a su tienda específica
INSERT INTO user_store_permissions (id, user_id, store_id)
VALUES
    (1, 2, 1), -- user_dinosaurio -> Shopping Dinosaurio Mall
    (2, 3, 2), -- user_maipu -> Centro Maipu 712
    (3, 4, 3); -- user_nuevo_centro -> Nuevo Centro Shopping

-- ===========================================
-- PRODUCTS (ropa)
-- ===========================================
INSERT INTO products (id, sku, name, description, is_active, created_at, updated_at)
VALUES
    (1, 'REM-001-BL-M', 'Remera Básica Blanca M', 'Remera de algodón peinado 160gsm', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'REM-002-NG-L', 'Remera Básica Negra L', 'Remera cuello redondo negra', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'REM-003-AZ-S', 'Remera Azul S', 'Remera azul marino corte clásico', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 'BUZ-001-GR-M', 'Buzo Hoodie Gris M', 'Buzo frisa liviano con capucha', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 'BUZ-002-NG-L', 'Buzo Hoodie Negro L', 'Buzo de algodón grueso con bolsillo canguro', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 'PAN-001-NG-32', 'Pantalón Chino Negro 32', 'Pantalón tipo chino con elastano', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7, 'PAN-002-AZ-34', 'Jeans Azul 34', 'Jeans corte slim fit azul oscuro', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8, 'BER-001-KH-M', 'Bermuda Cargo Khaki M', 'Bermuda con bolsillos laterales', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9, 'BER-002-AZ-L', 'Bermuda Denim Azul L', 'Bermuda de jean azul clásico', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, 'CAM-001-BL-M', 'Camisa Blanca Oxford M', 'Camisa manga larga oxford blanco', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, 'CAM-002-CE-L', 'Camisa Celeste L', 'Camisa de poplin celeste claro', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, 'CMP-001-NG-M', 'Campera Rompeviento Negra M', 'Campera liviana impermeable con cierre', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (13, 'CMP-002-AZ-L', 'Campera Puffer Azul L', 'Campera acolchada pluma sintética', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (14, 'ZAP-001-NG-42', 'Zapatillas Urbanas Negras 42', 'Zapatillas urbanas con suela EVA', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (15, 'ZAP-002-BL-41', 'Zapatillas Running Blancas 41', 'Zapatillas deportivas livianas', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (16, 'GOR-001-NG-U', 'Gorra Negra U', 'Gorra ajustable negra con visera curva', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (17, 'MED-001-BL-U', 'Pack Medias Blancas U', 'Pack x3 medias tobillera blancas', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ===========================================
-- INVENTORY
-- ===========================================
INSERT INTO inventory (id, product_id, store_id, available_qty, version, updated_at)
VALUES
    (1, 1, 1, 25, 0, CURRENT_TIMESTAMP),
    (2, 1, 2, 18, 0, CURRENT_TIMESTAMP),
    (3, 1, 3, 35, 0, CURRENT_TIMESTAMP),

    (4, 2, 1, 20, 0, CURRENT_TIMESTAMP),
    (5, 2, 2, 15, 0, CURRENT_TIMESTAMP),
    (6, 2, 3, 30, 0, CURRENT_TIMESTAMP),

    (7, 3, 1, 14, 0, CURRENT_TIMESTAMP),
    (8, 3, 2, 22, 0, CURRENT_TIMESTAMP),
    (9, 3, 3, 28, 0, CURRENT_TIMESTAMP),

    (10, 4, 1, 10, 0, CURRENT_TIMESTAMP),
    (11, 4, 2, 12, 0, CURRENT_TIMESTAMP),
    (12, 4, 3, 20, 0, CURRENT_TIMESTAMP),

    (13, 5, 1, 8, 0, CURRENT_TIMESTAMP),
    (14, 5, 2, 11, 0, CURRENT_TIMESTAMP),
    (15, 5, 3, 16, 0, CURRENT_TIMESTAMP),

    (16, 6, 1, 12, 0, CURRENT_TIMESTAMP),
    (17, 6, 2, 10, 0, CURRENT_TIMESTAMP),
    (18, 6, 3, 18, 0, CURRENT_TIMESTAMP),

    (19, 7, 1, 14, 0, CURRENT_TIMESTAMP),
    (20, 7, 2, 13, 0, CURRENT_TIMESTAMP),
    (21, 7, 3, 22, 0, CURRENT_TIMESTAMP),

    (22, 8, 1, 16, 0, CURRENT_TIMESTAMP),
    (23, 8, 2, 12, 0, CURRENT_TIMESTAMP),
    (24, 8, 3, 24, 0, CURRENT_TIMESTAMP),

    (25, 9, 1, 10, 0, CURRENT_TIMESTAMP),
    (26, 9, 2, 14, 0, CURRENT_TIMESTAMP),
    (27, 9, 3, 20, 0, CURRENT_TIMESTAMP),

    (28, 10, 1, 11, 0, CURRENT_TIMESTAMP),
    (29, 10, 2, 10, 0, CURRENT_TIMESTAMP),
    (30, 10, 3, 19, 0, CURRENT_TIMESTAMP),

    (31, 11, 1, 9, 0, CURRENT_TIMESTAMP),
    (32, 11, 2, 12, 0, CURRENT_TIMESTAMP),
    (33, 11, 3, 18, 0, CURRENT_TIMESTAMP),

    (34, 12, 1, 6, 0, CURRENT_TIMESTAMP),
    (35, 12, 2, 7, 0, CURRENT_TIMESTAMP),
    (36, 12, 3, 12, 0, CURRENT_TIMESTAMP),

    (37, 13, 1, 5, 0, CURRENT_TIMESTAMP),
    (38, 13, 2, 6, 0, CURRENT_TIMESTAMP),
    (39, 13, 3, 10, 0, CURRENT_TIMESTAMP),

    (40, 14, 1, 8, 0, CURRENT_TIMESTAMP),
    (41, 14, 2, 10, 0, CURRENT_TIMESTAMP),
    (42, 14, 3, 15, 0, CURRENT_TIMESTAMP),

    (43, 15, 1, 7, 0, CURRENT_TIMESTAMP),
    (44, 15, 2, 9, 0, CURRENT_TIMESTAMP),
    (45, 15, 3, 14, 0, CURRENT_TIMESTAMP),

    (46, 16, 1, 20, 0, CURRENT_TIMESTAMP),
    (47, 16, 2, 18, 0, CURRENT_TIMESTAMP),
    (48, 16, 3, 30, 0, CURRENT_TIMESTAMP),

    (49, 17, 1, 35, 0, CURRENT_TIMESTAMP),
    (50, 17, 2, 28, 0, CURRENT_TIMESTAMP),
    (51, 17, 3, 50, 0, CURRENT_TIMESTAMP);
