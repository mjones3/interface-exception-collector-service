package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.InventoryVolume;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class Inventory implements Validatable  {

    private final UUID id;
    private final String locationCode;
    private final String unitNumber;
    private final String productCode;
    private final String productDescription;
    private final LocalDateTime expirationDate;
    private final String aboRh;
    private final String productFamily;
    private final ZonedDateTime collectionDate;
    private final String storageLocation;
    private final ZonedDateTime createDate;
    private final ZonedDateTime modificationDate;
    private final Integer weight;
    private final List<InventoryVolume> volumes;
    private final String collectionLocation;
    private final String collectionTimeZone;

    public Inventory(UUID id, String locationCode, String unitNumber, String productCode, String productDescription, LocalDateTime expirationDate, String aboRh, String productFamily
        , ZonedDateTime collectionDate, String storageLocation, ZonedDateTime createDate, ZonedDateTime modificationDate , Integer weight , List<InventoryVolume> volumes , String collectionLocation , String collectionTimeZone ) {
        this.id = id;
        this.locationCode = locationCode;
        this.unitNumber = unitNumber;
        this.productCode = productCode;
        this.productDescription = productDescription;
        this.expirationDate = expirationDate;
        this.aboRh = aboRh;
        this.productFamily = productFamily;
        this.collectionDate = collectionDate;
        this.storageLocation = storageLocation;
        this.createDate = createDate;
        this.modificationDate = modificationDate;
        this.weight = weight;
        this.volumes = volumes;
        this.collectionLocation = collectionLocation;
        this.collectionTimeZone = collectionTimeZone;

        checkValid();
    }

    @Override
    public void checkValid() {
        if (this.id == null) {
            throw new IllegalArgumentException("Id is required");
        }

        if (this.locationCode == null || this.locationCode.isBlank()) {
            throw new IllegalArgumentException("Location Code is required");
        }

        if (this.unitNumber == null || this.unitNumber.isBlank()) {
            throw new IllegalArgumentException("Unit Number is required");
        }

        if (this.productCode == null || this.productCode.isBlank()) {
            throw new IllegalArgumentException("Product Code is required");
        }
        if(expirationDate == null){
            throw new IllegalArgumentException("Expiration Date is required");
        }
        if(aboRh == null || aboRh.isBlank()){
            throw new IllegalArgumentException("ABO/RH is required");
        }
        if(productFamily == null || productFamily.isBlank()){
            throw new IllegalArgumentException("Product Family is required");
        }
        if(collectionDate == null){
            throw new IllegalArgumentException("Collection Date is required");
        }
    }

    public Optional<InventoryVolume> getVolumeByType(String type){
        return this.volumes.stream().filter(volume -> volume.getType().equals(type)).findFirst();
    }
}
