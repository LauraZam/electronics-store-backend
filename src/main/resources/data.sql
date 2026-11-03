-- Clean table state (Optional)
TRUNCATE TABLE users, brands, categories, products, reviews, promo_codes, orders RESTART IDENTITY CASCADE;

-- 1. SEED USERS (Password for both: "password123" hashed with BCrypt)
INSERT INTO users (email, password, first_name, last_name, phone, role, enabled, email_verified, created_at)
VALUES
    ('admin@shop.local', '$2a$10$e8.Z/g1E1N8P1a11g7wO2.9dC7zS4P1M1.3/90fB4/5b2a0c410e2', 'Admin', 'User', '+1234567890', 'ADMIN', true, true, NOW()),
    ('customer@shop.local', '$2a$10$e8.Z/g1E1N8P1a11g7wO2.9dC7zS4P1M1.3/90fB4/5b2a0c410e2', 'John', 'Doe', '+1987654321', 'CUSTOMER', true, true, NOW());

-- 2. SEED BRANDS (3 Brands)
INSERT INTO brands (name, slug, description, country, is_active)
VALUES
    ('Apple', 'apple', 'Premium electronics manufacturer', 'USA', true),
    ('Samsung', 'samsung', 'Global technology provider', 'South Korea', true),
    ('Sony', 'sony', 'Leader in audio and entertainment devices', 'Japan', true);

-- 3. SEED CATEGORIES (5 Categories)
INSERT INTO categories (name, slug, description, sort_order, is_active, parent_id)
VALUES
    ('Electronics', 'electronics', 'Root electronics category', 1, true, NULL),
    ('Smartphones', 'smartphones', 'Mobile phones and handhelds', 2, true, 1),
    ('Laptops', 'laptops', 'Notebooks and portable computers', 3, true, 1),
    ('Audio', 'audio', 'Headphones and speakers', 4, true, 1),
    ('Wearables', 'wearables', 'Smartwatches and fitness trackers', 5, true, 1);

-- 4. SEED PRODUCTS (20 Products)
INSERT INTO products (name, sku, base_price, stock_quantity, reserved_quantity, is_active, is_featured, is_new_arrival, category_id, brand_id, average_rating, review_count, created_at)
VALUES
    ('iPhone 15 Pro', 'APL-IP15P-128', 999.00, 50, 0, true, true, true, 2, 1, 4.8, 2, NOW()),
    ('iPhone 15', 'APL-IP15-128', 799.00, 40, 0, true, false, true, 2, 1, 4.5, 1, NOW()),
    ('MacBook Pro 14 M3', 'APL-MBP14-M3', 1599.00, 20, 0, true, true, true, 3, 1, 5.0, 1, NOW()),
    ('MacBook Air 13 M2', 'APL-MBA13-M2', 1099.00, 30, 0, true, false, false, 3, 1, 4.7, 1, NOW()),
    ('AirPods Pro 2', 'APL-APP2', 249.00, 100, 0, true, true, false, 4, 1, 4.9, 1, NOW()),
    ('Apple Watch Series 9', 'APL-AWS9', 399.00, 45, 0, true, false, true, 5, 1, 4.6, 1, NOW()),

    ('Galaxy S24 Ultra', 'SAM-S24U-256', 1299.00, 35, 0, true, true, true, 2, 2, 4.7, 1, NOW()),
    ('Galaxy S24', 'SAM-S24-128', 799.00, 50, 0, true, false, true, 2, 2, 4.4, 1, NOW()),
    ('Galaxy Book4 Pro', 'SAM-GB4P', 1449.00, 15, 0, true, false, true, 3, 2, 4.3, 0, NOW()),
    ('Galaxy Buds2 Pro', 'SAM-GBP2', 199.00, 80, 0, true, false, false, 4, 2, 4.5, 1, NOW()),
    ('Galaxy Watch6', 'SAM-GW6', 299.00, 60, 0, true, false, false, 5, 2, 4.2, 0, NOW()),

    ('Sony WH-1000XM5', 'SNY-WH1000XM5', 399.00, 75, 0, true, true, false, 4, 3, 4.9, 1, NOW()),
    ('Sony WF-1000XM5', 'SNY-WF1000XM5', 299.00, 90, 0, true, false, false, 4, 3, 4.6, 0, NOW()),
    ('Sony Xperia 1 V', 'SNY-XP1V', 1199.00, 10, 0, true, false, false, 2, 3, 4.1, 0, NOW()),

    ('Generic USB-C Cable', 'GEN-USBC-1M', 15.00, 200, 0, true, false, false, 1, NULL, 4.0, 0, NOW()),
    ('Wireless Charging Pad', 'GEN-WCP-10W', 29.00, 150, 0, true, false, false, 1, NULL, 4.2, 0, NOW()),
    ('Bluetooth Speaker Mini', 'GEN-BSM', 49.00, 85, 0, true, false, false, 4, NULL, 4.0, 0, NOW()),
    ('Mechanical Keyboard', 'GEN-MK-RGB', 89.00, 40, 0, true, false, false, 1, NULL, 4.5, 0, NOW()),
    ('Ergonomic Wireless Mouse', 'GEN-EWM', 39.00, 65, 0, true, false, false, 1, NULL, 4.3, 0, NOW()),
    ('4K Monitor 27 Inch', 'GEN-MON-4K27', 349.00, 25, 0, true, false, false, 1, NULL, 4.7, 0, NOW());

-- 5. SEED PROMO CODES (3 Promo Codes)
INSERT INTO promo_code (code, description, discount_type, discount_value, min_order_amount, max_discount_amount, max_usage_count, current_usage_count, valid_from, valid_to, is_active)
VALUES
    ('WELCOME10', '10% off for new customers', 'PERCENTAGE', 10.00, 50.00, 50.00, 1000, 0, NOW(), NOW() + INTERVAL '30 days', true),
    ('FLAT20', '$20 off on orders over $100', 'FIXED_AMOUNT', 20.00, 100.00, 20.00, 500, 0, NOW(), NOW() + INTERVAL '30 days', true),
    ('FREESHIP', 'Free shipping offer', 'FREE_SHIPPING', 0.00, 30.00, 15.00, 200, 0, NOW(), NOW() + INTERVAL '30 days', true);

-- 6. SEED REVIEWS (10 Reviews)
INSERT INTO reviews (product_id, user_id, rating, title, comment, status, is_verified_purchase, created_at)
VALUES
    (1, 2, 5, 'Outstanding phone', 'Camera and performance are exceptional.', 'APPROVED', true, NOW()),
    (1, 2, 4, 'Great but pricey', 'Solid upgrade from my old phone.', 'APPROVED', true, NOW()),
    (2, 2, 5, 'Value for money', 'Great entry into the iOS ecosystem.', 'APPROVED', true, NOW()),
    (3, 2, 5, 'Unmatched speed', 'M3 chip handles heavy rendering with ease.', 'APPROVED', true, NOW()),
    (4, 2, 4, 'Ultra portable', 'Lightweight and powerful battery life.', 'APPROVED', true, NOW()),
    (5, 2, 5, 'Best ANC ever', 'Noise cancellation is top tier.', 'APPROVED', true, NOW()),
    (6, 2, 4, 'Nice fitness watch', 'Tracks health stats accurately.', 'APPROVED', true, NOW()),
    (7, 2, 5, 'Display is insane', 'Best display on any smartphone.', 'APPROVED', true, NOW()),
    (8, 2, 4, 'Solid flagship', 'Good compact size and display.', 'APPROVED', true, NOW()),
    (10, 2, 5, 'Great fit', 'Comfortable for long wearing sessions.', 'APPROVED', true, NOW());

-- 7. SEED ORDERS (5 Orders in different statuses)
INSERT INTO orders (order_number, user_id, status, subtotal, shipping_cost, discount, total_amount, delivery_method, created_at)
VALUES
    ('ORD-2026-0001', 2, 'PENDING', 999.00, 10.00, 0.00, 1009.00, 'COURIER', NOW()),
    ('ORD-2026-0002', 2, 'CONFIRMED', 249.00, 0.00, 20.00, 229.00, 'EXPRESS', NOW()),
    ('ORD-2026-0003', 2, 'PAID', 1599.00, 0.00, 50.00, 1549.00, 'COURIER', NOW()),
    ('ORD-2026-0004', 2, 'SHIPPED', 399.00, 5.00, 0.00, 404.00, 'PICKUP_POINT', NOW()),
    ('ORD-2026-0005', 2, 'DELIVERED', 799.00, 0.00, 10.00, 789.00, 'POST_OFFICE', NOW());

-- 8. SEED DELIVERY OPTIONS
INSERT INTO delivery_options (name, code, description, base_price, free_threshold, estimated_days_min, estimated_days_max, is_active)
VALUES
    ('Standard Courier', 'COURIER', 'Door-to-door courier delivery', 10.00, 100.00, 2, 5, true),
    ('Express Delivery', 'EXPRESS', 'Next day priority delivery', 25.00, 250.00, 1, 2, true),
    ('Pickup Point', 'PICKUP_POINT', 'Self-pickup from parcel locker', 5.00, 50.00, 3, 7, true),
    ('Post Office', 'POST_OFFICE', 'Standard postal service delivery', 7.00, 75.00, 4, 10, true);