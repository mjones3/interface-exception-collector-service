-- Create lk_product_determination table
CREATE TABLE IF NOT EXISTS irradiation.lk_product_determination (
    id SMALLSERIAL PRIMARY KEY,
    source_product_code VARCHAR(10) NOT NULL,
    target_product_code VARCHAR(10) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    create_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modification_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_lk_product_determination_source_target UNIQUE (source_product_code, target_product_code),
    CONSTRAINT uq_lk_product_determination_source UNIQUE (source_product_code)
);

-- Insert product determination data
INSERT INTO irradiation.lk_product_determination (source_product_code, target_product_code) VALUES
('E003300', 'E003200'),
('E018100', 'E017900'),
('E033600', 'E033200'),
('E038200', 'E037900'),
('E042400', 'E042000'),
('E068500', 'E066800'),
('E068600', 'E066900'),
('E070100', 'E070400'),
('E071300', 'E071600'),
('E355600', 'E354100'),
('E355800', 'E354300'),
('E355900', 'E354400'),
('E453100', 'E452600'),
('E453200', 'E452700'),
('E453300', 'E452800'),
('E414000', 'E456200'),
('E456600', 'E456200'),
('E456700', 'E456300'),
('E356000', 'E640800'),
('EA00700', 'EA01500'),
('EA00800', 'EA01600'),
('EA00900', 'EA01700'),
('EA01000', 'EA01800'),
('EA01100', 'EA01900'),
('EA01200', 'EA02000'),
('EA01300', 'EA02100'),
('EA01400', 'EA02200'),
('EA13600', 'EA15200')
ON CONFLICT (source_product_code) DO NOTHING;

-- Update sequence to prevent ID conflicts
SELECT setval('lk_product_determination_id_seq', (SELECT MAX(id) FROM irradiation.lk_product_determination));
