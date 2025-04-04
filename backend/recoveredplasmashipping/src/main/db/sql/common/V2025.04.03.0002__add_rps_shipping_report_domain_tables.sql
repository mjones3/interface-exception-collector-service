DROP VIEW IF EXISTS recoveredplasmashipping.VW_RECOVERED_PLASMA_SHIPMENT_REPORT ;

create or replace view recoveredplasmashipping.VW_RECOVERED_PLASMA_SHIPMENT_REPORT as
    select brps.id, brps.shipment_number ,brps.status , brps.shipment_date , brps.customer_code , brps.customer_name
         , brps.location_code , brps.transportation_reference_number, ll."name" as location , brps.product_type , lrppt.product_type_description
    from recoveredplasmashipping.bld_recovered_plasma_shipment brps
             left join recoveredplasmashipping.lk_location ll  on ll.code  = brps.location_code
             left join recoveredplasmashipping.lk_recovered_plasma_product_type lrppt  on lrppt.product_type = brps.product_type
    where brps.delete_date is null ;



