CREATE SCHEMA IF NOT EXISTS receiving;

CREATE TABLE receiving.lk_lookup (
    id                BIGSERIAL                NOT NULL CONSTRAINT pk_lk_lookup PRIMARY KEY,
    type              VARCHAR(50)              NOT NULL,
    description_key   VARCHAR(255)             NOT NULL,
    option_value      VARCHAR(255)             NOT NULL,
    order_number      INTEGER DEFAULT 1        NOT NULL,
    active            BOOLEAN                  NOT NULL
);

CREATE UNIQUE INDEX uq_idx_lk_lookup_type_option_value ON receiving.lk_lookup (type, option_value);
