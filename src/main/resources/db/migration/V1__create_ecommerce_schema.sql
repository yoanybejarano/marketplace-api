-- ============================================================
-- CUSTOMERS
-- ============================================================

CREATE TABLE customers (
                           id SERIAL PRIMARY KEY,
                           first_name VARCHAR(100) NOT NULL,
                           last_name VARCHAR(100) NOT NULL,
                           email VARCHAR(255) UNIQUE NOT NULL,
                           phone VARCHAR(30),
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- ============================================================
-- ADDRESSES
-- ============================================================

CREATE TABLE addresses (
                           id SERIAL PRIMARY KEY,
                           customer_id INT NOT NULL
                               REFERENCES customers(id)
                                   ON DELETE CASCADE,
                           street VARCHAR(255),
                           city VARCHAR(100),
                           state VARCHAR(100),
                           zip_code VARCHAR(20),
                           country VARCHAR(100),
                           is_default BOOLEAN DEFAULT FALSE
);


-- ============================================================
-- CATEGORIES
-- ============================================================

CREATE TABLE categories (
                            id SERIAL PRIMARY KEY,
                            name VARCHAR(100) UNIQUE NOT NULL,
                            description TEXT
);


-- ============================================================
-- PRODUCTS
--
-- stock_quantity is temporarily kept here.
-- It will be migrated to inventory in V4.
-- ============================================================

CREATE TABLE products (
                          id SERIAL PRIMARY KEY,
                          category_id INT REFERENCES categories(id),
                          name VARCHAR(255) NOT NULL,
                          description TEXT,
                          price NUMERIC(10,2) NOT NULL,
                          stock_quantity INT NOT NULL DEFAULT 0,
                          sku VARCHAR(50) UNIQUE NOT NULL,
                          image_url TEXT,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT chk_products_stock_quantity
                              CHECK (stock_quantity >= 0)
);


-- ============================================================
-- ORDERS
-- ============================================================

CREATE TABLE orders (
                        id SERIAL PRIMARY KEY,
                        customer_id INT NOT NULL
                            REFERENCES customers(id),
                        order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        status VARCHAR(30),
                        total_amount NUMERIC(10,2) DEFAULT 0,

                        CONSTRAINT chk_orders_total_amount
                            CHECK (total_amount >= 0)
);


-- ============================================================
-- ORDER ITEMS
--
-- location_id is intentionally NOT added yet.
-- Locations are created in V4.
-- It will be added and populated in V4.
-- ============================================================

CREATE TABLE order_items (
                             id SERIAL PRIMARY KEY,
                             order_id INT NOT NULL
                                 REFERENCES orders(id)
                                     ON DELETE CASCADE,
                             product_id INT NOT NULL
                                 REFERENCES products(id),
                             quantity INT NOT NULL,
                             unit_price NUMERIC(10,2) NOT NULL,

                             CONSTRAINT chk_order_items_quantity
                                 CHECK (quantity > 0),

                             CONSTRAINT chk_order_items_unit_price
                                 CHECK (unit_price >= 0)
);


-- ============================================================
-- PAYMENTS
-- ============================================================

CREATE TABLE payments (
                          id SERIAL PRIMARY KEY,
                          order_id INT NOT NULL UNIQUE,
                          payment_method VARCHAR(30) NOT NULL,
                          payment_status VARCHAR(30) NOT NULL,
                          transaction_id VARCHAR(100) NOT NULL UNIQUE,
                          gateway VARCHAR(50) NOT NULL,
                          gateway_message VARCHAR(255),
                          amount NUMERIC(12,2) NOT NULL,
                          currency CHAR(3) NOT NULL,
                          payment_date TIMESTAMP NOT NULL,
                          authorized_at TIMESTAMP,
                          captured_at TIMESTAMP,
                          refunded_at TIMESTAMP,

                          CONSTRAINT fk_payments_order
                              FOREIGN KEY (order_id)
                                  REFERENCES orders(id)
                                  ON DELETE CASCADE,

                          CONSTRAINT chk_payments_amount
                              CHECK (amount >= 0)
);


-- ============================================================
-- REVIEWS
-- ============================================================

CREATE TABLE reviews (
                         id SERIAL PRIMARY KEY,
                         customer_id INT
                             REFERENCES customers(id)
                                 ON DELETE CASCADE,
                         product_id INT
                             REFERENCES products(id)
                                 ON DELETE CASCADE,
                         rating INT
                             CHECK (rating BETWEEN 1 AND 5),
                         comment TEXT,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);