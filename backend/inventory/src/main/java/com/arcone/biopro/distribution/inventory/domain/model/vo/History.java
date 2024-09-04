package com.arcone.biopro.distribution.inventory.domain.model.vo;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"createDate"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class History implements Serializable, Comparable<History> {

    InventoryStatus inventoryStatus;

    CreatedDate createDate;

    String reason;

    public History(InventoryStatus inventoryStatus, String reason) {
        this.inventoryStatus = inventoryStatus;
        this.reason = reason;
        createDate = new CreatedDate(ZonedDateTime.now());
    }

    @Override
    public int compareTo( History o) {
        return o.getCreateDate().value().compareTo(createDate.value());
    }
}
