CREATE TABLE receiving.lk_barcode_pattern
(
    id                integer                  not null
        primary key,
    pattern           varchar(255)             not null,
    match_groups      integer                  not null,
    parse_type        varchar(255)             not null
);

COMMENT ON COLUMN receiving.lk_barcode_pattern.id IS 'Unique identifier for the barcode pattern';
COMMENT ON COLUMN receiving.lk_barcode_pattern.pattern IS 'Regular expression pattern used to match barcodes';
COMMENT ON COLUMN receiving.lk_barcode_pattern.match_groups IS 'Number of regex capture groups in the pattern';
COMMENT ON COLUMN receiving.lk_barcode_pattern.parse_type IS 'Type of parsing algorithm to apply (e.g., standard, gs1)';

CREATE TABLE receiving.lk_barcode_translation
(
    id                integer                  not null
        primary key,
    from_value        varchar(255)             not null,
    to_value          varchar(255)             not null,
    sixth_digit       varchar(1)
);

COMMENT ON COLUMN receiving.lk_barcode_translation.id IS 'Unique identifier for the translation rule';
COMMENT ON COLUMN receiving.lk_barcode_translation.from_value IS 'Original barcode value to be translated';
COMMENT ON COLUMN receiving.lk_barcode_translation.to_value IS 'Target value after translation';
COMMENT ON COLUMN receiving.lk_barcode_translation.sixth_digit IS 'Optional specific digit at position 6 for conditional translations';