-- ============================================================
-- V4 - INVENTORY BY LOCATION
-- ============================================================


-- ============================================================
-- LOCATIONS
-- ============================================================

CREATE TABLE locations (
                           id SERIAL PRIMARY KEY,
                           name VARCHAR(150) NOT NULL,
                           code VARCHAR(50) NOT NULL UNIQUE,
                           type VARCHAR(30) NOT NULL,
                           address VARCHAR(255),
                           city VARCHAR(100),
                           state VARCHAR(100),
                           zip_code VARCHAR(20),
                           country VARCHAR(100),
                           active BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT chk_location_type
                               CHECK (
                                   type IN (
                                            'STORE',
                                            'WAREHOUSE',
                                            'DISTRIBUTION_CENTER'
                                       )
                                   )
);


-- ============================================================
-- INVENTORY
-- ============================================================

CREATE TABLE inventory (
                           id SERIAL PRIMARY KEY,

                           product_id INT NOT NULL,
                           location_id INT NOT NULL,

    -- Physical quantity
                           quantity INT NOT NULL DEFAULT 0,

    -- Quantity reserved by pending orders
                           reserved_quantity INT NOT NULL DEFAULT 0,

    -- JPA optimistic locking
                           version BIGINT NOT NULL DEFAULT 0,

                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT fk_inventory_product
                               FOREIGN KEY (product_id)
                                   REFERENCES products(id)
                                   ON DELETE CASCADE,

                           CONSTRAINT fk_inventory_location
                               FOREIGN KEY (location_id)
                                   REFERENCES locations(id)
                                   ON DELETE CASCADE,

                           CONSTRAINT uq_inventory_product_location
                               UNIQUE (product_id, location_id),

                           CONSTRAINT chk_inventory_quantity
                               CHECK (quantity >= 0),

                           CONSTRAINT chk_inventory_reserved_quantity
                               CHECK (reserved_quantity >= 0),

                           CONSTRAINT chk_inventory_reserved_not_greater_than_quantity
                               CHECK (reserved_quantity <= quantity)
);


-- ============================================================
-- LOCATIONS SEED
-- ============================================================

INSERT INTO locations (
    name,
    code,
    type,
    address,
    city,
    state,
    zip_code,
    country
)
VALUES
    (
        'Main Warehouse',
        'WH-MAIN',
        'WAREHOUSE',
        '100 Industrial Avenue',
        'New York',
        'NY',
        '10001',
        'USA'
    ),
    (
        'New York Store',
        'STORE-NY',
        'STORE',
        '200 5th Avenue',
        'New York',
        'NY',
        '10001',
        'USA'
    ),
    (
        'Los Angeles Store',
        'STORE-LA',
        'STORE',
        '300 Sunset Boulevard',
        'Los Angeles',
        'CA',
        '90001',
        'USA'
    ),
    (
        'Chicago Distribution Center',
        'DC-CHI',
        'DISTRIBUTION_CENTER',
        '500 Logistics Drive',
        'Chicago',
        'IL',
        '60601',
        'USA'
    );


-- ============================================================
-- MIGRATE EXISTING STOCK TO LOCATIONS
--
-- The total quantity remains EXACTLY the same as the
-- original products.stock_quantity.
--
-- Distribution:
--
-- 40% -> Main Warehouse
-- 30% -> New York Store
-- 20% -> Los Angeles Store
-- remainder -> Chicago Distribution Center
--
-- Example:
--
-- stock = 50
--
-- WH-MAIN  = 20
-- STORE-NY = 15
-- STORE-LA = 10
-- DC-CHI   =  5
--
-- TOTAL = 50
-- ============================================================

INSERT INTO inventory (
    product_id,
    location_id,
    quantity,
    reserved_quantity,
    version
)
SELECT
    p.id,
    l.id,
    CASE l.code

        WHEN 'WH-MAIN'
            THEN FLOOR(p.stock_quantity * 0.40)::INT

        WHEN 'STORE-NY'
            THEN FLOOR(p.stock_quantity * 0.30)::INT

        WHEN 'STORE-LA'
            THEN FLOOR(p.stock_quantity * 0.20)::INT

        WHEN 'DC-CHI'
            THEN p.stock_quantity
                 - FLOOR(p.stock_quantity * 0.40)::INT
                 - FLOOR(p.stock_quantity * 0.30)::INT
                 - FLOOR(p.stock_quantity * 0.20)::INT

END AS quantity,

    0 AS reserved_quantity,

    0 AS version

FROM products p
CROSS JOIN locations l;


-- ============================================================
-- ORDER ITEM LOCATION
--
-- Existing order_items were created before locations existed.
-- Add the location after inventory has been created.
-- ============================================================

ALTER TABLE order_items
    ADD COLUMN location_id INT;


-- ============================================================
-- ASSIGN EXISTING ORDER ITEMS TO A LOCATION
--
-- For seed/demo orders we assign them to the New York Store.
--
-- In a real migration, this should come from historical
-- fulfillment/shipping data if available.
-- ============================================================

UPDATE order_items
SET location_id = (
    SELECT id
    FROM locations
    WHERE code = 'STORE-NY'
);


-- ============================================================
-- ORDER ITEM LOCATION FK
-- ============================================================

ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_location
        FOREIGN KEY (location_id)
            REFERENCES locations(id);


-- ============================================================
-- LOCATION IS NOW REQUIRED
-- ============================================================

ALTER TABLE order_items
    ALTER COLUMN location_id SET NOT NULL;


-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX idx_inventory_product
    ON inventory(product_id);

CREATE INDEX idx_inventory_location
    ON inventory(location_id);

CREATE INDEX idx_inventory_product_location
    ON inventory(product_id, location_id);

CREATE INDEX idx_locations_city
    ON locations(city);

CREATE INDEX idx_locations_active
    ON locations(active);

CREATE INDEX idx_order_items_location
    ON order_items(location_id);


-- ============================================================
-- REMOVE OLD GLOBAL STOCK
--
-- At this point all stock has been migrated into inventory.
-- ============================================================

-- Replace these lines at the bottom of V4:

-- SAFELY DROP CONSTRAINT IF IT EXISTS
ALTER TABLE products DROP CONSTRAINT IF EXISTS chk_products_stock_quantity;

-- DROP THE COLUMN
ALTER TABLE products DROP COLUMN stock_quantity;