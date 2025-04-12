CREATE TABLE recoveredplasmashipping.lk_recovered_plasma_shipment_criteria_item (
    recovered_plasma_shipment_criteria_id 			int4 NOT NULL,
    "type" 											varchar(100) NOT NULL,
    value 											varchar(225) NOT NULL,
    message											varchar(255) NULL,
    message_type    								varchar(100) NULL,
    CONSTRAINT lk_recovered_plasma_shipment_criteria_item_pkey
      PRIMARY KEY (recovered_plasma_shipment_criteria_id, type, value),
    CONSTRAINT fk_lk_recovered_plasma_shipment_criteria
      FOREIGN KEY(recovered_plasma_shipment_criteria_id)
          REFERENCES recoveredplasmashipping.lk_recovered_plasma_shipment_criteria(id)
);
