-- ============================================================
-- CUSTOMER
-- ============================================================

CREATE INDEX idx_customer_email ON customers(email);


-- ============================================================
-- PRODUCTS
-- ============================================================

CREATE INDEX idx_product_category ON products(category_id);


-- ============================================================
-- ORDERS
-- ============================================================

CREATE INDEX idx_order_customer ON orders(customer_id);


-- ============================================================
-- ORDER ITEMS
-- ============================================================

CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);


-- ============================================================
-- REVIEWS
-- ============================================================

CREATE INDEX idx_reviews_product ON reviews(product_id);