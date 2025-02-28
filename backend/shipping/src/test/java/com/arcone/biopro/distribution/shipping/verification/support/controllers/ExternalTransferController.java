package com.arcone.biopro.distribution.shipping.verification.support.controllers;

import com.arcone.biopro.distribution.shipping.verification.support.DatabaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component("AutomationExternalTransferController")
@Slf4j
public class ExternalTransferController {

    @Autowired
    private DatabaseService databaseService;

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public Long createProductLocationHistory(String customerCodeTo , String customerNameTo , String unitNumber , String productCode , String shippedDate){


        var shippedDateFormated = LocalDateTime.parse(shippedDate,DateTimeFormatter.ofPattern(DATE_FORMAT));

        var insertProductLocationHistory = "INSERT INTO bld_product_location_history (customer_code_to, customer_name_to " +
            " ,unit_number,product_code,history_type,created_by_employee_id,create_date,modification_date) " +
            " VALUES ('%s','%s','%s','%s','SHIPPING','employee-id-test','%s',now())";

        return databaseService.executeSql(String.format(insertProductLocationHistory, customerCodeTo,customerNameTo,unitNumber,productCode,shippedDateFormated)).block();

    }

}
