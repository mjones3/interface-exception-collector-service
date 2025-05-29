package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class RemoveCartonItemCommand implements Validatable {

    private Long cartonId;
    private String employeeId;
    private List<Long> cartonItemIds;

    public RemoveCartonItemCommand(Long cartonId, String employeeId, List<Long> cartonItemIds) {
        this.cartonId = cartonId;
        this.employeeId = employeeId;
        this.cartonItemIds = cartonItemIds;

        checkValid();
    }

    @Override
    public void checkValid() {
        if(cartonId == null){
            throw new IllegalArgumentException("Carton is required");
        }

        if(employeeId == null || employeeId.isBlank()){
            throw new IllegalArgumentException("Employee ID is required");
        }

        if(cartonItemIds == null || cartonItemIds.isEmpty()){
            throw new IllegalArgumentException("Carton items are required");
        }

    }
}
