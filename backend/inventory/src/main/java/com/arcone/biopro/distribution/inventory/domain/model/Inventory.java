package com.arcone.biopro.distribution.inventory.domain.model;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Volume;
import com.arcone.biopro.distribution.inventory.domain.model.vo.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    UUID id;

    UnitNumber unitNumber;

    ProductCode productCode;

    String shortDescription;

    InventoryStatus inventoryStatus;

    LocalDateTime expirationDate;

    ZonedDateTime collectionDate;

    Boolean isLicensed;

    Boolean isLabeled;

    Integer weight;

    String inventoryLocation;

    String collectionLocation;

    String productFamily;

    String statusReason;

    AboRhType aboRh;

    ZonedDateTime createDate;

    ZonedDateTime modificationDate;

    String collectionTimeZone;

    @Builder.Default
    List<Quarantine> quarantines = new ArrayList<>();

    @Builder.Default
    List<History> histories = new ArrayList<>();

    List<InputProduct> inputProducts;

    String comments;

    String deviceStored;

    String storageLocation;

    String unsuitableReason;

    String temperatureCategory;

    String cartonNumber;

    String modificationLocation;

    ZonedDateTime productModificationDate;


    @Builder.Default
    List<Volume> volumes = new ArrayList<>();

    public void createHistory() {
        histories.add(new History(inventoryStatus, statusReason, comments));
    }

    public void restoreHistory(){
        History history = new History(inventoryStatus, statusReason, comments);

        histories.stream().max(Comparator.comparing(History::createDate)).ifPresent(h -> {
                inventoryStatus = h.inventoryStatus();
                statusReason = h.reason();
                comments = h.comments();
        });

        histories.add(history);
    }

    public void addQuarantine(Long quarantineId, String reason, String comments) {
        quarantines.add(new Quarantine(quarantineId, reason, comments));
    }

    public void updateQuarantine(Long quarantineId, String reason, String comments) {
        quarantines = new ArrayList<>(quarantines.stream()
            .map(quarantine -> {
                if (quarantine.externId().equals(quarantineId)) {
                    return new Quarantine(quarantine.externId(), reason, comments);
                }
                return quarantine;
            })
            .toList());
    }

    public void removeQuarantine(Long quarantineId) {
        quarantines = new ArrayList<>(quarantines.stream().filter(q -> !q.externId().equals(quarantineId)).toList());
        if(quarantines.isEmpty()) {
            restoreHistory();
        }
    }

    public void transitionStatus(InventoryStatus newStatus, String statusReason) {
        createHistory();
        setStatusReason(statusReason);
        setInventoryStatus(newStatus);
    }

    public boolean isConverted() {
        return InventoryStatus.CONVERTED.equals(inventoryStatus);
    }

    public void addVolume(String type, Integer value, String unit) {
        volumes.add(new Volume(type, value, unit));
    }

    public Boolean isExpired() {
        return expirationDate != null && expirationDate.isBefore(LocalDateTime.now());
    }

    private <T> List<T> initializeIfNull(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }

    public Boolean isQuarantined() {
        return !initializeIfNull(quarantines).isEmpty();
    }

}
