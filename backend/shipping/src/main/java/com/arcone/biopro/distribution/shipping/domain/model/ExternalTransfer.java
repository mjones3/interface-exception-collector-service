package com.arcone.biopro.distribution.shipping.domain.model;

import com.arcone.biopro.distribution.shipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.shipping.application.exception.DomainException;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ExternalTransferStatus;
import com.arcone.biopro.distribution.shipping.domain.model.vo.Customer;
import com.arcone.biopro.distribution.shipping.domain.model.vo.Product;
import com.arcone.biopro.distribution.shipping.domain.repository.ProductLocationHistoryRepository;
import com.arcone.biopro.distribution.shipping.domain.service.CustomerService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private List<ExternalTransferItem> externalTransferItems;

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

    public ExternalTransfer(Long id , String customerCodeTo, String customerCodeFrom, String hospitalTransferId, LocalDate transferDate, String createEmployeeId, ExternalTransferStatus status , List<ExternalTransferItem> externalTransferItems , CustomerService customerService) {
        this(id , customerCodeTo, customerCodeFrom, hospitalTransferId, transferDate, createEmployeeId, status ,customerService);
        this.externalTransferItems = externalTransferItems;
    }

    public void addItem(Long itemId, String unitNumber, String productCode, String createEmployeeId , ProductLocationHistoryRepository productLocationHistoryRepository){
        if(externalTransferItems == null) {
            externalTransferItems = new ArrayList<>();
        }

        var product = new Product(unitNumber,productCode,null);

        var currentProductLocation = productLocationHistoryRepository.findCurrentLocation(product).blockOptional();
        if(currentProductLocation.isPresent()) {
            var productLocation = currentProductLocation.get();

            if(productLocation.getCreatedDate().toLocalDate().isAfter(this.transferDate)){
                throw new DomainException(UseCaseMessageType.EXTERNAL_TRANSFER_DATE_BEFORE_SHIP_DATE);
            }

            if(this.customerFrom != null && !productLocation.getCustomerTo().getCode().equals(this.customerFrom.getCode())){
                throw new DomainException(UseCaseMessageType.EXTERNAL_TRANSFER_LOCATION_DOES_NOT_MATCH);
            }

            this.externalTransferItems.add(new ExternalTransferItem(itemId,this.id,unitNumber,productCode,productLocation.getProduct().getProductFamily(),createEmployeeId));

            if(this.customerFrom == null){
                this.customerFrom = productLocation.getCustomerTo();
            }

        }else{
            throw new DomainException(UseCaseMessageType.EXTERNAL_TRANSFER_PRODUCT_NOT_SHIPPED );
        }
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
