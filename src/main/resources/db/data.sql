-- Очистка таблиц (для тестового окружения)
DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM cart_items;
DELETE FROM carts;
DELETE FROM products;

-- Сброс автоинкремента
ALTER TABLE products AUTO_INCREMENT = 1;
ALTER TABLE carts AUTO_INCREMENT = 1;
ALTER TABLE cart_items AUTO_INCREMENT = 1;
ALTER TABLE orders AUTO_INCREMENT = 1;
ALTER TABLE order_items AUTO_INCREMENT = 1;

-- Вставка тестовых товаров
INSERT INTO products (name, description, price, stock_quantity, category, image_url, sku, created_at, updated_at) VALUES
('MacBook Pro 16"', 'Мощный ноутбук для профессионалов с процессором M2 Pro', 2499.99, 15, 'Electronics', '/images/macbook-pro.jpg', 'MBP-16-2023', NOW(), NOW()),
('iPhone 15 Pro', 'Флагманский смартфон с камерой 48MP', 1199.99, 25, 'Electronics', '/images/iphone15-pro.jpg', 'IPHONE-15-PRO-256', NOW(), NOW()),
('Samsung Galaxy S24', 'Android смартфон с AI функциями', 899.99, 20, 'Electronics', '/images/galaxy-s24.jpg', 'SAMSUNG-S24-256', NOW(), NOW()),
('Dell XPS 13', 'Ультрабук с безрамочным дисплеем', 1299.99, 12, 'Electronics', '/images/dell-xps13.jpg', 'DELL-XPS13-2024', NOW(), NOW()),
('Spring Boot in Action', 'Полное руководство по Spring Boot разработке', 39.99, 50, 'Books', '/images/spring-boot-book.jpg', 'BOOK-SPRING-001', NOW(), NOW()),
('Effective Java', 'Лучшие практики программирования на Java', 49.99, 30, 'Books', '/images/effective-java.jpg', 'BOOK-JAVA-001', NOW(), NOW()),
('Sony WH-1000XM5', 'Беспроводные наушники с шумоподавлением', 349.99, 18, 'Electronics', '/images/sony-headphones.jpg', 'SONY-WH1000XM5', NOW(), NOW()),
('Apple Watch Series 9', 'Умные часы с функцией измерения ЭКГ', 399.99, 22, 'Electronics', '/images/apple-watch.jpg', 'APPLE-WATCH-S9', NOW(), NOW()),
('Clean Code', 'Руководство по написанию чистого кода', 34.99, 40, 'Books', '/images/clean-code.jpg', 'BOOK-CLEAN-CODE', NOW(), NOW()),
('Logitech MX Master 3', 'Эргономичная беспроводная мышь', 99.99, 35, 'Electronics', '/images/logitech-mouse.jpg', 'LOGITECH-MX-MASTER3', NOW(), NOW()),
('iPad Air', 'Планшет с чипом M1 и поддержкой Apple Pencil', 599.99, 16, 'Electronics', '/images/ipad-air.jpg', 'IPAD-AIR-5', NOW(), NOW()),
('Design Patterns', 'Классическая книга о шаблонах проектирования', 44.99, 28, 'Books', '/images/design-patterns.jpg', 'BOOK-DESIGN-PATTERNS', NOW(), NOW());

-- Тестовые корзины
INSERT INTO carts (session_id, total_price, total_items, created_at, updated_at) VALUES
('session-user-123', 0.00, 0, NOW(), NOW()),
('session-user-456', 0.00, 0, NOW(), NOW()),
('session-guest-789', 0.00, 0, NOW(), NOW());

-- Тестовые элементы корзины
INSERT INTO cart_items (cart_id, product_id, quantity, unit_price, created_at) VALUES
(1, 1, 1, 2499.99, NOW()),
(1, 3, 2, 899.99, NOW()),
(2, 5, 1, 39.99, NOW()),
(2, 6, 1, 49.99, NOW()),
(3, 7, 1, 349.99, NOW());

-- Обновление итогов корзин
UPDATE carts SET
total_price = (SELECT SUM(unit_price * quantity) FROM cart_items WHERE cart_id = 1),
total_items = (SELECT SUM(quantity) FROM cart_items WHERE cart_id = 1)
WHERE id = 1;

UPDATE carts SET
total_price = (SELECT SUM(unit_price * quantity) FROM cart_items WHERE cart_id = 2),
total_items = (SELECT SUM(quantity) FROM cart_items WHERE cart_id = 2)
WHERE id = 2;

UPDATE carts SET
total_price = (SELECT SUM(unit_price * quantity) FROM cart_items WHERE cart_id = 3),
total_items = (SELECT SUM(quantity) FROM cart_items WHERE cart_id = 3)
WHERE id = 3;

-- Тестовые заказы
INSERT INTO orders (order_number, status, total_amount, customer_name, customer_email, customer_phone, shipping_street, shipping_city, shipping_state, shipping_postal_code, shipping_country, created_at, updated_at) VALUES
('ORD-20240001', 'DELIVERED', 4299.97, 'Иван Петров', 'ivan.petrov@example.com', '+79161234567', 'ул. Ленина, д. 15', 'Москва', 'Московская область', '101000', 'Россия', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 1 DAY),
('ORD-20240002', 'PROCESSING', 89.98, 'Мария Сидорова', 'maria.sidorova@example.com', '+79269876543', 'пр. Победы, д. 42', 'Санкт-Петербург', 'Ленинградская область', '190000', 'Россия', NOW() - INTERVAL 2 DAY, NOW()),
('ORD-20240003', 'PENDING', 349.99, 'Алексей Козлов', 'alex.kozlov@example.com', '+79031112233', 'ул. Мира, д. 7', 'Казань', 'Татарстан', '420000', 'Россия', NOW() - INTERVAL 1 DAY, NOW());

-- Элементы заказов
INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, total_price, created_at) VALUES
(1, 1, 'MacBook Pro 16"', 2499.99, 1, 2499.99, NOW()),
(1, 3, 'Samsung Galaxy S24', 899.99, 2, 1799.98, NOW()),
(2, 5, 'Spring Boot in Action', 39.99, 1, 39.99, NOW()),
(2, 6, 'Effective Java', 49.99, 1, 49.99, NOW()),
(3, 7, 'Sony WH-1000XM5', 349.99, 1, 349.99, NOW());
