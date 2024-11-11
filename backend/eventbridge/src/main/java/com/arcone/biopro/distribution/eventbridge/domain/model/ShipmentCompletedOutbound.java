package com.arcone.biopro.distribution.eventbridge.domain.model;

import com.arcone.biopro.distribution.eventbridge.domain.model.vo.ShipmentCustomer;
import com.arcone.biopro.distribution.eventbridge.domain.model.vo.ShipmentLocation;
import com.arcone.biopro.distribution.eventbridge.domain.model.vo.ShipmentService;
import lombok.Getter;
import org.springframework.util.Assert;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


/*
payload
    externalOrderID*
    customerCode*
    customerType*
    customerName --- remove
    shipmentNumber*
    shipmentDate*
    quantityShipped*
    shipmentLocationCode*
    shipmentLocationName*
    lineItem*
        productFamily*
        qtyOrdered*
        qtyFilled*
        products*
            unitNumber*
            productCode*
            bloodType*
            expirationDate*
            attributes [ "key", "value" ]*
            collectionDate*
    services*
        serviceItemCode*
        quantity*
*/


@Getter
public class ShipmentCompletedOutbound {

    private final Long shipmentId;
    private final String externalId;
    private final ZonedDateTime shipmentDate;
    private final ShipmentCustomer shipmentCustomer;
    private final ShipmentLocation shipmentLocation;

    private List<ShipmentLineItem> lineItems;
    private List<ShipmentService> services;

    public ShipmentCompletedOutbound(Long shipmentId, String externalId, ZonedDateTime shipmentDate
        , ShipmentCustomer shipmentCustomer , ShipmentLocation shipmentLocation) {

        Assert.notNull(shipmentId, "shipmentId must not be null");
        Assert.notNull(externalId, "externalId must not be null");
        Assert.notNull(shipmentDate, "shipmentDate must not be null");
        Assert.notNull(shipmentCustomer, "Customer must not be null");
        Assert.notNull(shipmentLocation, "Location must not be null");

        this.shipmentId = shipmentId;
        this.externalId = externalId;
        this.shipmentDate = shipmentDate;
        this.shipmentCustomer = shipmentCustomer;
        this.shipmentLocation = shipmentLocation;
    }

    public void addLineItem(final ShipmentLineItem shipmentLineItem) {
        if (lineItems == null) {
            lineItems = new ArrayList<>();
        }

        lineItems.add(shipmentLineItem);
    }

    public void addService(String serviceCode , Integer quantity) {
        if (services == null) {
            services = new ArrayList<>();
        }
        services.add(new ShipmentService(serviceCode,quantity));
    }

    public Integer getQuantityShipped() {
        if(this.lineItems != null){
            return this.lineItems.stream()
                .map(ShipmentLineItem::getQuantityFilled)
                .reduce(0, Integer::sum);
        }
        return 0;
    }

}
