package com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.client;

public class InventoryOutput {
    private String unitNumber;
    private String productCode;
    private String location;
    private String status;

    public InventoryOutput() {}

    public InventoryOutput(String unitNumber, String productCode, String location, String status) {
        this.unitNumber = unitNumber;
        this.productCode = productCode;
        this.location = location;
        this.status = status;
    }

    public String getUnitNumber() {
        return unitNumber;
    }

    public void setUnitNumber(String unitNumber) {
        this.unitNumber = unitNumber;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}