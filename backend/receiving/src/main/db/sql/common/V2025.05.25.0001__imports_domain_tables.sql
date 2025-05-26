CREATE TABLE receiving.lk_product_consequence (
    id BIGINT NOT NULL,
    product_category VARCHAR(255) NOT NULL,
    acceptable boolean NOT NULL,
    result_property VARCHAR(255) NOT NULL,
    result_type VARCHAR(255) NOT NULL,
    result_value VARCHAR(255) NOT NULL,
    consequence_type VARCHAR(255) NOT NULL,
    consequence_reason VARCHAR(255) NOT NULL,
    order_number INT NOT NULL DEFAULT 1,
    active boolean NOT NULL DEFAULT true,
    create_date DATETIME NOT NULL,
    modification_date DATETIME NOT NULL,
    PRIMARY KEY (id)
);

COMMENT ON TABLE receiving.lk_product_consequence IS 'Stores the configurations of the validations that needs to be applied in the receiving process.';

COMMENT ON COLUMN receiving.lk_product_consequence.id IS 'Primary key';
COMMENT ON COLUMN receiving.lk_product_consequence.product_category IS 'Product category';
COMMENT ON COLUMN receiving.lk_product_consequence.acceptable IS 'Is the result value acceptable? true/false values';
COMMENT ON COLUMN receiving.lk_product_consequence.result_property IS 'Product consequence field';
COMMENT ON COLUMN receiving.lk_product_consequence.result_type IS 'Product consequence result type';
COMMENT ON COLUMN receiving.lk_product_consequence.result_value IS 'Product consequence result value';
COMMENT ON COLUMN receiving.lk_product_consequence.consequence_type IS 'Product consequence type';
COMMENT ON COLUMN receiving.lk_product_consequence.consequence_reason_key IS 'Product consequence reason';
COMMENT ON COLUMN receiving.lk_product_consequence.order_number IS 'Display order for this table';
COMMENT ON COLUMN receiving.lk_product_consequence.active IS 'Is the product consequence active? true/false values';
COMMENT ON COLUMN receiving.lk_product_consequence.create_date IS 'Date the product consequence was created';
COMMENT ON COLUMN receiving.lk_product_consequence.modification_date IS 'Date the product consequence was modified';
