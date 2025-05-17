package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ProductType;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Validatable;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class ShippingSummaryShipmentDetail implements Validatable {

    private String shipmentNumber;
    private String productType;
    private final String productCode;
    private final int totalNumberOfCartons;
    private final int totalNumberOfProducts;
    private final String transportationReferenceNumber;
    private final boolean displayTransportationNumber;

    public ShippingSummaryShipmentDetail(final RecoveredPlasmaShipment recoveredPlasmaShipment, final List<ShippingSummaryCartonItem> cartonList , String displayTransportationNumberConfiguration , RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository ) {

        this.setShipmentDetails(recoveredPlasmaShipment);
        this.productType = getProductType(recoveredPlasmaShipment.getProductType(), recoveredPlasmaShipmentCriteriaRepository);
        this.productCode = getProductCodes(cartonList);
        this.totalNumberOfCartons = cartonList != null && !cartonList.isEmpty() ? cartonList.size() : 0;
        this.totalNumberOfProducts = getTotalNumberOfProducts(cartonList);

        this.transportationReferenceNumber = recoveredPlasmaShipment.getTransportationReferenceNumber();
        this.displayTransportationNumber = "Y".equals(displayTransportationNumberConfiguration) ? Boolean.TRUE : Boolean.FALSE;

        checkValid();

    }


    @Override
    public void checkValid() {

        if(shipmentNumber == null || shipmentNumber.isBlank()){
            throw new IllegalArgumentException("Shipment Number is required");
        }

        if(productType == null || productType.isBlank()){
            throw new IllegalArgumentException("Product Type is required");
        }

    }

    private void setShipmentDetails(RecoveredPlasmaShipment recoveredPlasmaShipment){
        if(recoveredPlasmaShipment == null){
            throw new IllegalArgumentException("Recovered Plasma Shipment is required");
        }

        this.shipmentNumber = recoveredPlasmaShipment.getShipmentNumber();
        this.productType = recoveredPlasmaShipment.getProductType();
    }

    private int getTotalNumberOfProducts(List<ShippingSummaryCartonItem> cartonList) {
        if(cartonList == null || cartonList.isEmpty()){
            return 0;
        }
        return cartonList.stream().mapToInt(ShippingSummaryCartonItem::getTotalProducts).sum();
    }

    private String getProductCodes(final List<ShippingSummaryCartonItem> cartonList) {
        if(cartonList == null || cartonList.isEmpty()){
            return "";
        }
        return cartonList.stream()
            .map(ShippingSummaryCartonItem::getProductCode)
            .distinct()
            .reduce((productCode1, productCode2) -> productCode1 + ", " + productCode2)
            .orElse("");
    }

    private String getProductType(String productType, RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository) {

        if(recoveredPlasmaShipmentCriteriaRepository == null){
            throw new IllegalArgumentException("Recovered Plasma Shipment Criteria Repository is required");
        }

        return recoveredPlasmaShipmentCriteriaRepository.findBYProductType(productType)
            .switchIfEmpty(Mono.error( ()-> new IllegalArgumentException("Product Type is required")))
            .map(ProductType::getProductTypeDescription)
            .block();
    }
}

