CREATE TABLE bld_order (
    -- order
                           id SERIAL NOT NULL,
                           order_number int4 NOT NULL,
                           location_code int8 NULL,
                           delivery_type varchar(255) NOT NULL,
                           shipping_method varchar(255) NOT NULL,
                           product_category varchar(255) NOT NULL,
                           shipping_date DATE NOT NULL,
                           status varchar(255) NOT NULL,
                           priority varchar(255) NOT NULL,
                           create_date timestamptz NOT NULL,
    --- customer
                           shipping_customer_code int8 NULL,
                           shipping_customer_name varchar(255) NOT NULL,
                           billing_customer_name varchar(255) NOT NULL,
                           billing_customer_code int8 NULL,
                           customer_phone_number varchar(255) NULL,
    --- customer address
                           customer_address_contact_name varchar(255) NULL,
                           customer_address_state varchar(50) NOT NULL,
                           customer_address_postal_code varchar(10) NOT NULL,
                           customer_address_country varchar(10) NOT NULL,
                           customer_address_country_code varchar(10) NOT NULL,
                           customer_address_city varchar(255) NOT NULL,
                           customer_address_district varchar(50) NULL,
                           customer_address_line1 varchar(255) NOT NULL,
                           customer_address_line2 varchar(255) NULL,
                           CONSTRAINT pk_bld_order PRIMARY KEY (id)
);

CREATE TABLE bld_order_item (
                                id SERIAL NOT NULL,
                                order_id int8 NOT NULL,
                                product_family varchar(255) NOT NULL,
                                blood_type varchar(255) NOT NULL,
                                quantity int4 NOT NULL,
                                "comments" varchar(1000) NULL,
                                create_date timestamptz NOT NULL,
                                CONSTRAINT pk_bld_order_item PRIMARY KEY (id),
                                CONSTRAINT fk_order_order_item FOREIGN KEY (order_id) REFERENCES bld_order(id)
);
