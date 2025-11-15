-- SLG Inventory - Seed Data and Sample Data
-- This file contains initial data for the application

-- ===== CLEANING EXISTING DATA =====
DELETE FROM vendor_inventory;
DELETE FROM center_inventory;
DELETE FROM vendors;
DELETE FROM users;
DELETE FROM goods;

-- ===== GOODS DATA =====
INSERT INTO goods (id, name) VALUES
(1, 'Rice'),
(2, 'Big Onions'),
(3, 'Potatoes'),
(4, 'Tomatoes'),
(5, 'Carrots'),
(6, 'Cabbage'),
(7, 'Pumpkin'),
(8, 'Brinjal'),
(9, 'Green Chilies'),
(10, 'Lentils'),
(11, 'Sugar'),
(12, 'Wheat Flour'),
(13, 'Coconut Oil'),
(14, 'Tea'),
(15, 'Spices');

-- ===== SAMPLE USERS AND VENDORS =====
-- Password for all sample users: 'password123'
INSERT INTO users (id, username, password_hash, company) VALUES
(1, 'lakshman_perera', '$2a$12$K9RlB2b/8E1QzW7cY6p8YuJkLmNOPqXr8WjJkLmNOPqXr8WjJkLmNO', 'Lakshman Traders'),
(2, 'kamal_fernando', '$2a$12$K9RlB2b/8E1QzW7cY6p8YuJkLmNOPqXr8WjJkLmNOPqXr8WjJkLmNO', 'Fernando Wholesale'),
(3, 'sujeewa_silva', '$2a$12$K9RlB2b/8E1QzW7cY6p8YuJkLmNOPqXr8WjJkLmNOPqXr8WjJkLmNO', 'Silva Enterprises'),
(4, 'nimal_ratnayake', '$2a$12$K9RlB2b/8E1QzW7cY6p8YuJkLmNOPqXr8WjJkLmNOPqXr8WjJkLmNO', 'Ratnayake Distributors');

INSERT INTO vendors (user_id, name, phone) VALUES
(1, 'Lakshman Perera', '+94 77 123 4567'),
(2, 'Kamal Fernando', '+94 71 234 5678'),
(3, 'Sujeewa Silva', '+94 76 345 6789'),
(4, 'Nimal Ratnayake', '+94 70 456 7890');

-- ===== CENTER INVENTORY DATA =====
-- Pettah Center (Main Colombo Center)
INSERT INTO center_inventory (center_code, goods_id, quantity) VALUES
('PETTAH', 1, 12500),   -- Rice
('PETTAH', 2, 8500),    -- Big Onions
('PETTAH', 3, 9200),    -- Potatoes
('PETTAH', 4, 4500),    -- Tomatoes
('PETTAH', 5, 3200),    -- Carrots
('PETTAH', 6, 2800),    -- Cabbage
('PETTAH', 7, 1800),    -- Pumpkin
('PETTAH', 8, 2200),    -- Brinjal
('PETTAH', 9, 1500),    -- Green Chilies
('PETTAH', 10, 6800),   -- Lentils
('PETTAH', 11, 4200),   -- Sugar
('PETTAH', 12, 5800),   -- Wheat Flour
('PETTAH', 13, 2500),   -- Coconut Oil
('PETTAH', 14, 3500),   -- Tea
('PETTAH', 15, 1800);   -- Spices

-- Peliyagoda Center (New Manning Market)
INSERT INTO center_inventory (center_code, goods_id, quantity) VALUES
('PELIYAGODA', 1, 9800),
('PELIYAGODA', 2, 7200),
('PELIYAGODA', 3, 8100),
('PELIYAGODA', 4, 5200),
('PELIYAGODA', 5, 2800),
('PELIYAGODA', 6, 3200),
('PELIYAGODA', 7, 2200),
('PELIYAGODA', 8, 1800),
('PELIYAGODA', 9, 1200),
('PELIYAGODA', 10, 4500),
('PELIYAGODA', 11, 3500),
('PELIYAGODA', 12, 4200),
('PELIYAGODA', 13, 1800),
('PELIYAGODA', 14, 2800),
('PELIYAGODA', 15, 1500);

-- Dambulla Center (Central Province Hub)
INSERT INTO center_inventory (center_code, goods_id, quantity) VALUES
('DAMBULLA', 1, 11500),
('DAMBULLA', 2, 9500),
('DAMBULLA', 3, 8800),
('DAMBULLA', 4, 6800),
('DAMBULLA', 5, 4200),
('DAMBULLA', 6, 3800),
('DAMBULLA', 7, 3200),
('DAMBULLA', 8, 2800),
('DAMBULLA', 9, 1800),
('DAMBULLA', 10, 5200),
('DAMBULLA', 11, 3800),
('DAMBULLA', 12, 4800),
('DAMBULLA', 13, 2200),
('DAMBULLA', 14, 3200),
('DAMBULLA', 15, 1200);

-- Kandy Center
INSERT INTO center_inventory (center_code, goods_id, quantity) VALUES
('KANDY', 1, 8200),
('KANDY', 2, 5800),
('KANDY', 3, 6500),
('KANDY', 4, 3800),
('KANDY', 5, 2500),
('KANDY', 6, 2200),
('KANDY', 7, 1500),
('KANDY', 8, 1200),
('KANDY', 9, 800),
('KANDY', 10, 3200),
('KANDY', 11, 2800),
('KANDY', 12, 3500),
('KANDY', 13, 1500),
('KANDY', 14, 2200),
('KANDY', 15, 900);

-- Galle Center
INSERT INTO center_inventory (center_code, goods_id, quantity) VALUES
('GALLE', 1, 7200),
('GALLE', 2, 4800),
('GALLE', 3, 5200),
('GALLE', 4, 3200),
('GALLE', 5, 1800),
('GALLE', 6, 1500),
('GALLE', 7, 1200),
('GALLE', 8, 1000),
('GALLE', 9, 600),
('GALLE', 10, 2800),
('GALLE', 11, 2200),
('GALLE', 12, 3000),
('GALLE', 13, 1200),
('GALLE', 14, 1800),
('GALLE', 15, 700);

-- Jaffna Center
INSERT INTO center_inventory (center_code, goods_id, quantity) VALUES
('JAFFNA', 1, 6800),
('JAFFNA', 2, 4200),
('JAFFNA', 3, 4800),
('JAFFNA', 4, 2800),
('JAFFNA', 5, 1500),
('JAFFNA', 6, 1200),
('JAFFNA', 7, 1000),
('JAFFNA', 8, 800),
('JAFFNA', 9, 500),
('JAFFNA', 10, 2200),
('JAFFNA', 11, 1800),
('JAFFNA', 12, 2500),
('JAFFNA', 13, 1000),
('JAFFNA', 14, 1500),
('JAFFNA', 15, 600);

-- ===== VENDOR INVENTORY DATA =====
-- Lakshman Perera's inventory
INSERT INTO vendor_inventory (vendor_id, goods_id, quantity) VALUES
(1, 1, 2500),   -- Rice
(1, 2, 1200),   -- Big Onions
(1, 3, 1800),   -- Potatoes
(1, 10, 800),   -- Lentils
(1, 11, 600),   -- Sugar
(1, 12, 900);   -- Wheat Flour

-- Kamal Fernando's inventory
INSERT INTO vendor_inventory (vendor_id, goods_id, quantity) VALUES
(2, 4, 1500),   -- Tomatoes
(2, 5, 800),    -- Carrots
(2, 6, 700),    -- Cabbage
(2, 7, 500),    -- Pumpkin
(2, 8, 600),    -- Brinjal
(2, 9, 400);    -- Green Chilies

-- Sujeewa Silva's inventory
INSERT INTO vendor_inventory (vendor_id, goods_id, quantity) VALUES
(3, 13, 800),   -- Coconut Oil
(3, 14, 1200),  -- Tea
(3, 15, 500),   -- Spices
(3, 1, 1800),   -- Rice
(3, 10, 600);   -- Lentils

-- Nimal Ratnayake's inventory
INSERT INTO vendor_inventory (vendor_id, goods_id, quantity) VALUES
(4, 2, 900),    -- Big Onions
(4, 3, 1100),   -- Potatoes
(4, 4, 800),    -- Tomatoes
(4, 11, 400),   -- Sugar
(4, 12, 700);   -- Wheat Flour

-- ===== SAMPLE TRANSACTION DATA (Optional - for future expansion) =====
-- CREATE TABLE IF NOT EXISTS transactions (
--     id INTEGER PRIMARY KEY AUTOINCREMENT,
--     vendor_id INTEGER,
--     center_code TEXT,
--     goods_id INTEGER,
--     quantity INTEGER,
--     type TEXT CHECK(type IN ('STOCK_IN', 'STOCK_OUT', 'TRANSFER')),
--     transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP,
--     FOREIGN KEY(vendor_id) REFERENCES vendors(id),
--     FOREIGN KEY(goods_id) REFERENCES goods(id)
-- );

-- ===== INDEXES FOR PERFORMANCE =====
CREATE INDEX IF NOT EXISTS idx_center_inventory_center ON center_inventory(center_code);
CREATE INDEX IF NOT EXISTS idx_center_inventory_goods ON center_inventory(goods_id);
CREATE INDEX IF NOT EXISTS idx_vendor_inventory_vendor ON vendor_inventory(vendor_id);
CREATE INDEX IF NOT EXISTS idx_vendor_inventory_goods ON vendor_inventory(goods_id);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_vendors_user ON vendors(user_id);

-- ===== SAMPLE QUERIES FOR REFERENCE =====
-- Get total inventory by center
-- SELECT center_code, SUM(quantity) as total_quantity 
-- FROM center_inventory 
-- GROUP BY center_code 
-- ORDER BY total_quantity DESC;

-- Get vendor inventory with goods names
-- SELECT v.name as vendor_name, g.name as goods_name, vi.quantity
-- FROM vendor_inventory vi
-- JOIN vendors v ON v.id = vi.vendor_id
-- JOIN goods g ON g.id = vi.goods_id
-- ORDER BY v.name, vi.quantity DESC;

-- Get low stock items (below 1000 units)
-- SELECT center_code, g.name as goods_name, ci.quantity
-- FROM center_inventory ci
-- JOIN goods g ON g.id = ci.goods_id
-- WHERE ci.quantity < 1000
-- ORDER BY ci.quantity ASC;

PRAGMA foreign_keys = ON;