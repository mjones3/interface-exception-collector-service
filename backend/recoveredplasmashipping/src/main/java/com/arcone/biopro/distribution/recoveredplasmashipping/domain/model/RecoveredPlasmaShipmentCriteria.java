package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.RecoveredPlasmaShipmentCriteriaItem;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class RecoveredPlasmaShipmentCriteria implements Validatable {

    private final Integer id;
    private final String customerCode;
    private final String productType;
    private final List<RecoveredPlasmaShipmentCriteriaItem> criteriaItemList;

    public RecoveredPlasmaShipmentCriteria(Integer id, String customerCode, String productType , List<RecoveredPlasmaShipmentCriteriaItem> criteriaItemList) {
        this.id = id;
        this.customerCode = customerCode;
        this.productType = productType;
        this.criteriaItemList = criteriaItemList;
        checkValid();
    }

    @Override
    public void checkValid() {

        if (this.id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }

        if (this.customerCode == null || this.customerCode.isBlank()) {
            throw new IllegalArgumentException("Customer code cannot be null or blank");
        }

        if (this.productType == null || this.productType.isBlank()) {
            throw new IllegalArgumentException("Product type cannot be null or blank");
        }

    }

    public Optional<RecoveredPlasmaShipmentCriteriaItem> findCriteriaItemByType(String type){
        if (this.criteriaItemList == null){
            return Optional.empty();
        }
        return this.criteriaItemList.stream().filter(item -> item.getType().equals(type)).findFirst();
    }
}
