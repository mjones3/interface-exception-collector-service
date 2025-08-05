package com.arcone.biopro.distribution.shipping.domain.model;

import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ProductLocationHistoryType;
import com.arcone.biopro.distribution.shipping.domain.model.vo.Customer;
import com.arcone.biopro.distribution.shipping.domain.model.vo.Product;
import com.arcone.biopro.distribution.shipping.domain.service.CustomerService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.ZonedDateTime;

@Getter
@EqualsAndHashCode
@ToString
public class ProductLocationHistory implements Validatable {

    private final Long id;
    private Customer customerTo;
    private Customer customerFrom;
    private final String type;
    private final Product product;
    private final String createdByEmployeeId;
    private ZonedDateTime createdDate;
    private final CustomerService customerService;

    public ProductLocationHistory(Long id, String customerCodeTo , String customerNameTo
        , String customerCodeFrom ,  String customerNameFrom, String type, String unitNumber , String productCode , String productFamily, String createdByEmployeeId , ZonedDateTime createdDate , CustomerService customerService) {
        this.id = id;
        this.customerService = customerService;

        if(customerCodeTo != null) {
            this.customerTo = new Customer(customerCodeTo,customerNameTo,customerService);
        }

        if(customerCodeFrom != null) {
            this.customerFrom = new Customer(customerCodeFrom,customerNameFrom,customerService);
        }
        this.type = type;
        this.product = new Product(unitNumber, productCode,productFamily);
        this.createdByEmployeeId = createdByEmployeeId;
        this.createdDate = createdDate;

        checkValid();
    }

    @Override
    public void checkValid() {

        if (this.customerTo == null) {
            throw new IllegalArgumentException("Customer To cannot be null");
        }

        if (this.product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        if (this.createdByEmployeeId == null || this.createdByEmployeeId.isBlank()) {
            throw new IllegalArgumentException("Created Employee cannot be null or blank");
        }

        if (this.type == null || this.type.isBlank()) {
            throw new IllegalArgumentException("Type cannot be null or blank");
        }

        try {
           ProductLocationHistoryType.valueOf(this.type);

        }catch (Exception e){
            throw new IllegalArgumentException("Type "+type+" is not a valid product location history type");
        }

        if(ProductLocationHistoryType.EXTERNAL_TRANSFER.equals(ProductLocationHistoryType.valueOf(this.type)) && this.customerFrom == null){
            throw new IllegalArgumentException("Customer From cannot be null");
        }
    }
}
