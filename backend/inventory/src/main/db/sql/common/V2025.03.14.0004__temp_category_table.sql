CREATE TABLE inventory.lk_temperature_category_product_code
(
    product_code            VARCHAR(50)              NOT NULL,
    temperature_category    VARCHAR(50)              NOT NULL,
    active                  BOOLEAN DEFAULT TRUE     NOT NULL,
    create_date             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    modification_date       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT pk_lk_temperature_category_product_code PRIMARY KEY (product_code)
);

ALTER TABLE inventory.bld_inventory ADD COLUMN temperature_category VARCHAR(50) NULL;

-- migrate script

-- ALTER TABLE inventory.bld_inventory ALTER COLUMN temperature_category SET NOT NULL;

INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E002300', 'REFRIGERATED');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E003300', 'REFRIGERATED');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E011200', 'REFRIGERATED');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E016700', 'REFRIGERATED');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E018100', 'REFRIGERATED');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E026200', 'REFRIGERATED');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E031600', 'REFRIGERATED');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E033600', 'REFRIGERATED');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E036600', 'REFRIGERATED');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E038200', 'REFRIGERATED');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E040400', 'REFRIGERATED');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E042400', 'REFRIGERATED');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E067800', 'REFRIGERATED');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E068500', 'REFRIGERATED');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E068600', 'REFRIGERATED');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E070100', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E071300', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E086900', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E0869A0', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E0869B0', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E0869C0', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E0869D0', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E162400', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E1624A0', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E1624B0', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E1624C0', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E1624D0', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E245700', 'REFRIGERATED');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E246900', 'REFRIGERATED');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E255500', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E261900', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E453100', 'REFRIGERATED');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E453200', 'REFRIGERATED');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E453300', 'REFRIGERATED');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E468900', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E469300', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E469700', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E470100', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E760700', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E763700', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E763900', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E764100', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E764300', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E764400', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E7644A0', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E7644B0', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E7644C0', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E7644D0', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E764600', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E764800', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E765000', 'FROZEN');





