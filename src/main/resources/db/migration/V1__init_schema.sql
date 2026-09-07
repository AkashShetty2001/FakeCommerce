CREATE TABLE IF NOT EXISTS categories(
    id BIGINT NOT NULL AUTO_INCREMENT,
    category_name VARCHAR(255) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    deleted_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS products(
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(38, 2) NOT NULL,
    category_id BIGINT NOT NULL,
    image VARCHAR(255),
    ratings VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    deleted_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    status SMALLINT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    deleted_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS order_products(
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    deleted_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES products(id)
);