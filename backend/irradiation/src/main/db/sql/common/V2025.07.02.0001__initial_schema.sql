CREATE SCHEMA IF NOT EXISTS irradiation;


CREATE TABLE irradiation.lk_configuration(
                                          key varchar(255) NOT NULL,
                                          value varchar(255) NOT NULL,
                                          CONSTRAINT pk_lk_configuration PRIMARY KEY (key)
);
