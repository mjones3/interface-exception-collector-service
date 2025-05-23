CREATE TABLE recoveredplasmashipping.lk_label_template (
    id int4 NOT NULL,
    "template" TEXT NOT NULL,
    order_number int4 NOT NULL,
    create_date timestamptz NOT NULL,
    modification_date timestamptz NOT NULL,
    template_type varchar(100) NOT NULL,
    active BOOLEAN NOT NULL,
    CONSTRAINT pk_lk_label_template PRIMARY KEY (id)
);




