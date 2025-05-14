package com.arcone.biopro.distribution.inventory.infrastructure.persistence;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.vo.History;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Quarantine;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Volume;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@EqualsAndHashCode(exclude = {"createDate", "modificationDate"})
@Table("bld_inventory")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryEntity implements Serializable, Persistable<UUID> {

    @Id
    @Column("id")
    UUID id;

    @NotBlank
    @Column("unit_number")
    String unitNumber;

    @NotBlank
    @Column("product_code")
    String productCode;

    @NotBlank
    @Column("short_description")
    String shortDescription;

    @NotNull
    @Column("status")
    InventoryStatus inventoryStatus;

    @NotNull
    @Column("expiration_date")
    LocalDateTime expirationDate;

    @NotNull
    @Column("collection_date")
    ZonedDateTime collectionDate;

    @NotNull
    @Column("is_licensed")
    Boolean isLicensed;

    @NotNull
    @Column("weight")
    Integer weight;

    @NotNull
    @Column("inventory_location")
    String inventoryLocation;

    @NotNull
    @Column("product_family")
    String productFamily;

    @NotNull
    @Column("abo_rh")
    AboRhType aboRh;

    @NotNull
    @Column("create_date")
    @CreatedDate
    @InsertOnlyProperty
    ZonedDateTime createDate;

    @NotNull
    @Column("modification_date")
    @LastModifiedDate
    ZonedDateTime modificationDate;

    @Column("status_reason")
    String statusReason;

    @Column("quarantines")
    List<Quarantine> quarantines;

    @Column("histories")
    List<History> histories;

    @Column("device_stored")
    String deviceStored;

    @Column("storage_location")
    String storageLocation;

    @Column("comments")
    String comments;

    @NotNull
    @Column("is_labeled")
    Boolean isLabeled;

    @NotNull
    @Column("unsuitable_reason")
    String unsuitableReason;

    @Column("temperature_category")
    String temperatureCategory;

    @Column("carton_number")
    String cartonNumber;

    @Column("collection_location")
    String collectionLocation;

    @Column("collection_timezone")
    String collectionTimeZone;

    @Column("modification_location")
    String modificationLocation;

    @Column("product_modification_date")
    ZonedDateTime productModificationDate;

    @Column("volumes")
    List<Volume> volumes;

    @JsonIgnore
    @Override
    public boolean isNew() {
        return createDate == null;
    }
}
