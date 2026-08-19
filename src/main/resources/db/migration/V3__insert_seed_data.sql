-- INSERT CATEGORIES
INSERT INTO categories(name, description) VALUES
                                              ('Electronics','Electronic Devices'),
                                              ('Books','Books and Literature'),
                                              ('Clothing','Fashion and Apparel'),
                                              ('Home','Home and Kitchen'),
                                              ('Sports','Sports Equipment'),
                                              ('Toys','Kids Toys');

-- INSERT CUSTOMERS
INSERT INTO customers(first_name,last_name,email,phone) VALUES
                                                            ('John','Doe','john@example.com','111111111'),
                                                            ('Jane','Smith','jane@example.com','222222222'),
                                                            ('Robert','Johnson','robert@example.com','333333333'),
                                                            ('Emily','Brown','emily@example.com','444444444'),
                                                            ('Michael','Wilson','michael@example.com','555555555'),
                                                            ('Sophia','Taylor','sophia@example.com','666666666'),
                                                            ('David','Martinez','david@example.com','777777777'),
                                                            ('Olivia','Anderson','olivia@example.com','888888888'),
                                                            ('Daniel','Thomas','daniel@example.com','999999999'),
                                                            ('Emma','Jackson','emma@example.com','123456789');

-- INSERT ADDRESSES
INSERT INTO addresses(customer_id,street,city,state,zip_code,country,is_default) VALUES
                                                                                     (1,'123 Main St','New York','NY','10001','USA',TRUE),
                                                                                     (2,'456 Oak Ave','Los Angeles','CA','90001','USA',TRUE),
                                                                                     (3,'789 Pine Rd','Chicago','IL','60601','USA',TRUE),
                                                                                     (4,'321 Cedar Blvd','Houston','TX','77001','USA',TRUE),
                                                                                     (5,'654 Elm Street','Phoenix','AZ','85001','USA',TRUE),
                                                                                     (6,'777 Lake View','Seattle','WA','98101','USA',TRUE),
                                                                                     (7,'987 Maple St','Miami','FL','33101','USA',TRUE),
                                                                                     (8,'555 Sunset Ave','Denver','CO','80014','USA',TRUE),
                                                                                     (9,'111 Green Rd','Boston','MA','02108','USA',TRUE),
                                                                                     (10,'900 Ocean Blvd','San Diego','CA','92101','USA',TRUE);

-- INSERT PRODUCTS
INSERT INTO products(category_id, name, description, price, stock_quantity, sku, image_url) VALUES
    (1,'Apple iPhone 15','128GB Smartphone',999.99,50,'IPH15-128','iphone15.jpg'),
    (1,'Samsung Galaxy S24','Android Phone',899.99,40,'SGS24','galaxy.jpg'),
    (1,'Apple MacBook Air M3','Laptop',1499.99,20,'MBA-M3','macbook.jpg'),
    (1,'Dell XPS 15','Laptop',1799.99,15,'DXPS15','dell.jpg'),
    (1,'Sony WH1000XM5','Noise Cancelling Headphones',399.99,60,'SONYXM5','sony.jpg'),
    (2,'Clean Code','Programming Book',39.99,120,'BOOK001','cleancode.jpg'),
    (2,'Effective Java','Programming Book',45.99,80,'BOOK002','effectivejava.jpg'),
    (2,'Spring in Action','Spring Boot Book',49.99,70,'BOOK003','spring.jpg'),
    (2,'Design Patterns','Software Engineering',59.99,40,'BOOK004','designpatterns.jpg'),
    (3,'Nike Air Max','Running Shoes',129.99,90,'NIKE01','nike.jpg'),
    (3,'Levis Jeans','Blue Jeans',69.99,150,'LEVIS01','jeans.jpg'),
    (3,'Adidas Hoodie','Sports Hoodie',59.99,110,'ADI01','hoodie.jpg'),
    (4,'Coffee Maker','Kitchen Appliance',89.99,30,'HOME01','coffee.jpg'),
    (4,'Air Fryer','Kitchen Appliance',149.99,40,'HOME02','airfryer.jpg'),
    (4,'Vacuum Cleaner','Home Appliance',199.99,25,'HOME03','vacuum.jpg'),
    (5,'Football','Professional Football',29.99,200,'SPORT01','football.jpg'),
    (5,'Basketball','NBA Basketball',34.99,180,'SPORT02','basketball.jpg'),
    (5,'Mountain Bike Helmet','Helmet',79.99,60,'SPORT03','helmet.jpg'),
    (6,'LEGO City Set','Kids Building Set',59.99,90,'TOY01','lego.jpg'),
    (6,'Remote Control Car','RC Car',99.99,75,'TOY02','rc.jpg');

-- INSERT ORDERS
INSERT INTO orders(customer_id,status,total_amount) VALUES
                                                        (1,'PAID',1039.98),
                                                        (2,'SHIPPED',129.99),
                                                        (3,'DELIVERED',1799.99),
                                                        (4,'PROCESSING',199.98),
                                                        (5,'PAID',239.98),
                                                        (6,'DELIVERED',399.99),
                                                        (7,'SHIPPED',59.99),
                                                        (8,'PAID',1499.99),
                                                        (9,'PROCESSING',89.99),
                                                        (10,'DELIVERED',79.99);

-- INSERT ORDER ITEMS
INSERT INTO order_items(order_id,product_id,quantity,unit_price) VALUES
                                                                     (1,1,1,999.99),
                                                                     (1,6,1,39.99),
                                                                     (2,10,1,129.99),
                                                                     (3,4,1,1799.99),
                                                                     (4,20,2,99.99),
                                                                     (5,13,1,89.99),
                                                                     (5,16,5,29.99),
                                                                     (6,5,1,399.99),
                                                                     (7,19,1,59.99),
                                                                     (8,3,1,1499.99),
                                                                     (9,13,1,89.99),
                                                                     (10,18,1,79.99);

-- INSERT PAYMENTS
INSERT INTO payments (order_id, payment_method, payment_status, transaction_id, gateway, gateway_message, amount, currency, payment_date, authorized_at, captured_at, refunded_at) VALUES
                                                                                                                                                                                       (1, 'CREDIT_CARD','CAPTURED', 'PAY-000001', 'FAKE_GATEWAY', 'Payment captured successfully', 1299.99, 'USD', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                       (2, 'PAYPAL', 'CAPTURED', 'PAY-000002', 'FAKE_GATEWAY', 'Payment captured successfully', 249.50, 'USD', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                       (3, 'DEBIT_CARD', 'AUTHORIZED', 'PAY-000003', 'FAKE_GATEWAY', 'Payment authorized', 599.99, 'USD', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, NULL),
                                                                                                                                                                                       (4, 'CREDIT_CARD', 'PENDING', 'PAY-000004', 'FAKE_GATEWAY', 'Waiting for customer confirmation', 89.95, 'USD', CURRENT_TIMESTAMP, NULL, NULL, NULL),
                                                                                                                                                                                       (5, 'PAYPAL', 'DECLINED', 'PAY-000005', 'FAKE_GATEWAY', 'Insufficient funds', 149.99, 'USD', CURRENT_TIMESTAMP, NULL, NULL, NULL),
                                                                                                                                                                                       (6, 'GOOGLE_PAY', 'FAILED', 'PAY-000006', 'FAKE_GATEWAY', 'Payment gateway timeout', 39.99, 'USD', CURRENT_TIMESTAMP, NULL, NULL, NULL),
                                                                                                                                                                                       (7, 'APPLE_PAY', 'REFUNDED', 'PAY-000007', 'FAKE_GATEWAY', 'Refund completed', 499.00, 'USD', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                                                                                                       (8, 'CREDIT_CARD', 'CAPTURED', 'PAY-000008', 'FAKE_GATEWAY', 'Payment captured successfully', 79.99, 'USD', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                       (9, 'PAYPAL', 'CANCELLED', 'PAY-000009', 'FAKE_GATEWAY', 'Payment cancelled by customer', 159.99, 'USD', CURRENT_TIMESTAMP, NULL, NULL, NULL),
                                                                                                                                                                                       (10, 'DEBIT_CARD', 'CAPTURED', 'PAY-000010', 'FAKE_GATEWAY', 'Payment captured successfully', 999.99, 'USD', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL);

-- INSERT REVIEWS
INSERT INTO reviews(customer_id,product_id,rating,comment) VALUES
                                                               (1,1,5,'Amazing phone!'),
                                                               (2,10,4,'Very comfortable shoes.'),
                                                               (3,4,5,'Excellent laptop.'),
                                                               (4,20,5,'Kids love it.'),
                                                               (5,13,4,'Makes excellent coffee.'),
                                                               (6,5,5,'Best headphones ever.'),
                                                               (7,19,5,'Great LEGO set.'),
                                                               (8,3,5,'Very fast laptop.'),
                                                               (9,16,4,'Nice football.'),
                                                               (10,18,5,'Comfortable helmet.');