package com.arcone.biopro.distribution.shipping.domain.model;

import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ExternalTransferStatus;
import com.arcone.biopro.distribution.shipping.domain.model.vo.Customer;
import com.arcone.biopro.distribution.shipping.domain.service.CustomerService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@EqualsAndHashCode
@ToString
public class ExternalTransfer implements Validatable{

    private final Long id;
    private Customer customerTo;
    private Customer customerFrom;
    private final String hospitalTransferId;
    private final LocalDate transferDate;
    private final String createEmployeeId;
    private final ExternalTransferStatus status;
    private final CustomerService customerService;

    public ExternalTransfer(Long id , String customerCodeTo, String customerCodeFrom, String hospitalTransferId, LocalDate transferDate, String createEmployeeId, ExternalTransferStatus status , CustomerService customerService) {
        this.id = id;
        this.hospitalTransferId = hospitalTransferId;
        this.transferDate = transferDate;
        this.createEmployeeId = createEmployeeId;
        this.status = status;
        this.customerTo = new Customer(customerCodeTo,null,customerService);
        if(customerCodeFrom != null && !customerCodeFrom.isBlank()) {
            this.customerFrom = new Customer(customerCodeFrom,null,customerService);
        }

        this.customerService = customerService;

        checkValid();
    }

    @Override
    public void checkValid() {

        if (this.customerTo == null) {
            throw new IllegalArgumentException("Customer To cannot be null");
        }

        if(this.transferDate == null) {
            throw new IllegalArgumentException("Transfer Date cannot be null");
        }

        if(this.transferDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Transfer Date cannot be in the future");
        }

        if(this.status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        if (this.createEmployeeId == null || this.createEmployeeId.isBlank()) {
            throw new IllegalArgumentException("Create Employee ID cannot be null or blank");
        }

    }
}
